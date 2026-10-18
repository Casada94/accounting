package com.asadaclan.accounting.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.scheduling.annotation.Scheduled;

import com.asadaclan.accounting.entities.Business;
import com.asadaclan.accounting.entities.Expense;
import com.asadaclan.accounting.entities.Payout;
import com.asadaclan.accounting.entities.ReportRequest;
import com.asadaclan.accounting.entities.Transaction;
import com.asadaclan.accounting.models.MonthlyReport;
import com.asadaclan.accounting.models.YearlyReport;
import com.asadaclan.accounting.producers.EmailProducer;
import com.asadaclan.accounting.reporting.FinancialReportGenerator;
import com.asadaclan.accounting.repositories.BusinessRepository;
import com.asadaclan.accounting.repositories.ExpenseRepository;
import com.asadaclan.accounting.repositories.PayoutRepository;
import com.asadaclan.accounting.repositories.ReportRepository;
import com.asadaclan.accounting.repositories.TransactionRepository;
import com.asadaclan.accounting.util.DateParser;

class ReportServiceTests {

    private static final String BUSINESS_ID = "+15555550100";
    private static final String BUSINESS_EMAIL = "owner@example.com";
    private static final Business BUSINESS = new Business(BUSINESS_ID, "Example Studio", BUSINESS_EMAIL);

    private ReportRepository reportRepository;
    private FinancialReportGenerator reportGenerator;
    private TransactionRepository transactionRepository;
    private ExpenseRepository expenseRepository;
    private PayoutRepository payoutRepository;
    private BusinessRepository businessRepository;
    private EmailProducer emailProducer;
    private DateParser dateParser;
    private ReportService reportService;

    @TempDir
    Path tempDirectory;

    @BeforeEach
    void setUp() {
        reportRepository = mock(ReportRepository.class);
        reportGenerator = mock(FinancialReportGenerator.class);
        transactionRepository = mock(TransactionRepository.class);
        expenseRepository = mock(ExpenseRepository.class);
        payoutRepository = mock(PayoutRepository.class);
        businessRepository = mock(BusinessRepository.class);
        emailProducer = mock(EmailProducer.class);
        dateParser = new DateParser(Clock.fixed(
                Instant.parse("2027-01-01T07:00:00Z"), ZoneId.of("America/Chicago")));
        reportService = new ReportService(
                reportRepository, reportGenerator, transactionRepository, expenseRepository,
                payoutRepository, businessRepository, emailProducer, dateParser);
    }

    @Test
    void monthlyScheduleGeneratesPreviousMonthForEveryBusiness() {
        when(businessRepository.findAll()).thenReturn(List.of(BUSINESS));
        ReportService spy = spy(reportService);
        doNothing().when(spy).generateReport(any(Business.class), any(ReportRequest.class));

        spy.generateMonthlyReport();

        ArgumentCaptor<ReportRequest> reportCaptor = ArgumentCaptor.forClass(ReportRequest.class);
        verify(spy).generateReport(eq(BUSINESS), reportCaptor.capture());
        assertThat(reportCaptor.getValue().businessId()).isEqualTo(BUSINESS_ID);
        assertThat(reportCaptor.getValue().startDate()).isEqualTo(LocalDate.of(2026, 12, 1));
        assertThat(reportCaptor.getValue().endDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void monthlyScheduleUsesConfiguredCronAndTimeZone() throws Exception {
        Method scheduledMethod = ReportService.class.getMethod("generateMonthlyReport");
        Scheduled scheduled = scheduledMethod.getAnnotation(Scheduled.class);

        assertThat(scheduled).isNotNull();
        assertThat(scheduled.cron()).isEqualTo("0 0 1 1 * *");
        assertThat(scheduled.zone()).isEqualTo("${accounting.time-zone}");
    }

    @Test
    void yearlyScheduleGeneratesPreviousYearForEveryBusiness() {
        when(businessRepository.findAll()).thenReturn(List.of(BUSINESS));
        ReportService spy = spy(reportService);
        doNothing().when(spy).generateReport(any(Business.class), any(ReportRequest.class));

        spy.generateYearlyReport();

        ArgumentCaptor<ReportRequest> reportCaptor = ArgumentCaptor.forClass(ReportRequest.class);
        verify(spy).generateReport(eq(BUSINESS), reportCaptor.capture());
        assertThat(reportCaptor.getValue().businessId()).isEqualTo(BUSINESS_ID);
        assertThat(reportCaptor.getValue().startDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(reportCaptor.getValue().endDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void yearlyScheduleUsesConfiguredCronAndTimeZone() throws Exception {
        Method scheduledMethod = ReportService.class.getMethod("generateYearlyReport");
        Scheduled scheduled = scheduledMethod.getAnnotation(Scheduled.class);

        assertThat(scheduled).isNotNull();
        assertThat(scheduled.cron()).isEqualTo("0 0 1 1 1 *");
        assertThat(scheduled.zone()).isEqualTo("${accounting.time-zone}");
    }

    @Test
    void buildMonthLoadsAllMatchingTypesAndCalculatesTotals() {
        YearMonth month = YearMonth.of(2026, 2);
        Transaction transaction = new Transaction(
                new BigDecimal("100.00"), new BigDecimal("10.00"), "Client", LocalDate.of(2026, 2, 3));
        Expense expense = new Expense(new BigDecimal("20.00"), "Ink", LocalDate.of(2026, 2, 4));
        Payout payout = new Payout(new BigDecimal("30.00"), LocalDate.of(2026, 2, 5));
        when(transactionRepository.findAllNotUndoneByBusinessIdAndDateBetweenOrderByDateAsc(
                BUSINESS_ID, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)))
                .thenReturn(List.of(transaction));
        when(expenseRepository.findAllNotUndoneByBusinessIdAndDateBetweenOrderByDateAsc(
                BUSINESS_ID, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)))
                .thenReturn(List.of(expense));
        when(payoutRepository.findAllNotUndoneByBusinessIdAndDateBetweenOrderByDateAsc(
                BUSINESS_ID, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)))
                .thenReturn(List.of(payout));

        MonthlyReport report = reportService.buildMonth(BUSINESS_ID, month);

        assertThat(report.transactions()).containsExactly(transaction);
        assertThat(report.expenses()).containsExactly(expense);
        assertThat(report.payouts()).containsExactly(payout);
        assertThat(report.totals().getGrossIncome()).isEqualByComparingTo("80.00");
        assertThat(report.totals().getTakeHome()).isEqualByComparingTo("70.00");
        assertThat(report.totals().getPayouts()).isEqualByComparingTo("30.00");
    }

    @Test
    void buildYearRunsMonthlyFlowTwelveTimesAndExposesNestedLists() {
        YearlyReport report = reportService.buildYear(BUSINESS_ID, Year.of(2024));

        assertThat(report.months()).hasSize(12);
        assertThat(report.months().get(0).period()).isEqualTo(YearMonth.of(2024, 1));
        assertThat(report.months().get(11).period()).isEqualTo(YearMonth.of(2024, 12));
        assertThat(report.transactions()).hasSize(12).allSatisfy(entries -> assertThat(entries).isEmpty());
        assertThat(report.expenses()).hasSize(12).allSatisfy(entries -> assertThat(entries).isEmpty());
        assertThat(report.payouts()).hasSize(12).allSatisfy(entries -> assertThat(entries).isEmpty());
        verify(transactionRepository, times(12))
                .findAllNotUndoneByBusinessIdAndDateBetweenOrderByDateAsc(any(), any(), any());
        verify(expenseRepository, times(12))
                .findAllNotUndoneByBusinessIdAndDateBetweenOrderByDateAsc(any(), any(), any());
        verify(payoutRepository, times(12))
                .findAllNotUndoneByBusinessIdAndDateBetweenOrderByDateAsc(any(), any(), any());
        verify(transactionRepository).findAllNotUndoneByBusinessIdAndDateBetweenOrderByDateAsc(
                BUSINESS_ID, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29));
    }

    @Test
    void yearlyRequestGeneratesAndEmailsOnlyOneYearlyReport() throws Exception {
        ReportRequest request = new ReportRequest(
                null, BUSINESS_ID, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        File file = Files.createTempFile(tempDirectory, "yearly-report-", ".pdf").toFile();
        when(reportGenerator.generate(any(YearlyReport.class))).thenReturn(file);

        reportService.generateReport(BUSINESS, request);

        verify(reportGenerator).generate(any(YearlyReport.class));
        verify(reportGenerator, times(0)).generate(any(MonthlyReport.class));
        verify(emailProducer).sendMail(
                BUSINESS_EMAIL, "2026 Financial Report", "Your report is attached.", file);
        verify(reportRepository).save(request);
        assertThat(file).doesNotExist();
    }

    @Test
    void rejectsAReportRequestForADifferentBusiness() {
        ReportRequest request = new ReportRequest(
                null, "+15555550199", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertThatThrownBy(() -> reportService.generateReport(BUSINESS, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must match");

        verifyNoInteractions(reportGenerator, emailProducer, reportRepository);
    }
}
