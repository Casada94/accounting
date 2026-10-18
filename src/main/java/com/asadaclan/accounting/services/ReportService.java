package com.asadaclan.accounting.services;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.asadaclan.accounting.entities.Business;
import com.asadaclan.accounting.entities.Expense;
import com.asadaclan.accounting.entities.Payout;
import com.asadaclan.accounting.entities.ReportRequest;
import com.asadaclan.accounting.entities.Transaction;
import com.asadaclan.accounting.exceptions.ReportException;
import com.asadaclan.accounting.models.MonthlyReport;
import com.asadaclan.accounting.models.Total;
import com.asadaclan.accounting.models.YearlyReport;
import com.asadaclan.accounting.producers.EmailProducer;
import com.asadaclan.accounting.reporting.FinancialReportGenerator;
import com.asadaclan.accounting.repositories.BusinessRepository;
import com.asadaclan.accounting.repositories.ExpenseRepository;
import com.asadaclan.accounting.repositories.PayoutRepository;
import com.asadaclan.accounting.repositories.ReportRepository;
import com.asadaclan.accounting.repositories.TransactionRepository;
import com.asadaclan.accounting.util.DateParser;
import com.asadaclan.accounting.util.FileUtil;

@Service
public class ReportService {

    private final EmailProducer emailProducer;
    private final ReportRepository reportRepository;
    private final FinancialReportGenerator reportGenerator;
    private final TransactionRepository transactionRepo;
    private final ExpenseRepository expenseRepo;
    private final PayoutRepository payoutRepo;
    private final BusinessRepository businessRepository;
    private final DateParser dateParser;

    public ReportService(ReportRepository reportRepository,
            FinancialReportGenerator reportGenerator,
            TransactionRepository transactionRepo,
            ExpenseRepository expenseRepo,
            PayoutRepository payoutRepo,
            BusinessRepository businessRepository,
            EmailProducer emailProducer,
            DateParser dateParser) {
        this.reportRepository = reportRepository;
        this.reportGenerator = reportGenerator;
        this.transactionRepo = transactionRepo;
        this.expenseRepo = expenseRepo;
        this.payoutRepo = payoutRepo;
        this.businessRepository = businessRepository;
        this.emailProducer = emailProducer;
        this.dateParser = dateParser;
    }

    @Scheduled(cron = "0 0 1 1 * *", zone = "${accounting.time-zone}")
    public void generateMonthlyReport() {
        YearMonth targetMonth = YearMonth.from(dateParser.businessDate()).minusMonths(1);
        for (Business business : businessRepository.findAll()) {
            generateReport(business, new ReportRequest(
                    null, business.phoneNumber(), targetMonth.atDay(1), targetMonth.atEndOfMonth()));
        }
    }

    @Scheduled(cron = "0 0 1 1 1 *", zone = "${accounting.time-zone}")
    public void generateYearlyReport() {
        Year targetYear = Year.from(dateParser.businessDate()).minusYears(1);
        for (Business business : businessRepository.findAll()) {
            generateReport(business, new ReportRequest(
                    null, business.phoneNumber(), targetYear.atDay(1), targetYear.atMonth(12).atEndOfMonth()));
        }
    }

    public void generateReport(Business business, ReportRequest request) {
        Objects.requireNonNull(business, "business must not be null");
        Objects.requireNonNull(request, "report request must not be null");
        String businessId = Objects.requireNonNull(business.phoneNumber(), "business phone number must not be null");
        if (!businessId.equals(request.businessId())) {
            throw new IllegalArgumentException("report businessId must match the supplied business");
        }
        String recipient = Objects.requireNonNull(business.email(), "business email must not be null");
        if (recipient.isBlank()) {
            throw new IllegalArgumentException("business email must not be blank");
        }

        File reportFile = null;
        try {
            String subject;
            if (isCompleteMonth(request)) {
                MonthlyReport report = buildMonth(businessId, YearMonth.from(request.startDate()));
                reportFile = reportGenerator.generate(report);
                subject = report.period() + " Financial Report";
            } else if (isCompleteYear(request)) {
                YearlyReport report = buildYear(businessId, Year.from(request.startDate()));
                reportFile = reportGenerator.generate(report);
                subject = report.year() + " Financial Report";
            } else {
                throw new IllegalArgumentException("Report range must be one complete calendar month or year");
            }

            emailProducer.sendMail(recipient, subject, "Your report is attached.", reportFile);
            reportRepository.save(request);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ReportException("Report generation was interrupted", exception);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ReportException("Unable to generate report", exception);
        } finally {
            FileUtil.deleteIfExists(reportFile);
        }
    }

    public MonthlyReport buildMonth(String businessId, YearMonth month) {
        Objects.requireNonNull(businessId, "businessId must not be null");
        Objects.requireNonNull(month, "month must not be null");

        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();
        List<Transaction> transactions = transactionRepo
                .findAllNotUndoneByBusinessIdAndDateBetweenOrderByDateAsc(businessId, startDate, endDate);
        List<Expense> expenses = expenseRepo
                .findAllNotUndoneByBusinessIdAndDateBetweenOrderByDateAsc(businessId, startDate, endDate);
        List<Payout> payouts = payoutRepo
                .findAllNotUndoneByBusinessIdAndDateBetweenOrderByDateAsc(businessId, startDate, endDate);

        return new MonthlyReport(month, transactions, expenses, payouts,
                calculateTotals(transactions, expenses, payouts));
    }

    public YearlyReport buildYear(String businessId, Year year) {
        Objects.requireNonNull(businessId, "businessId must not be null");
        Objects.requireNonNull(year, "year must not be null");

        List<MonthlyReport> months = new ArrayList<>(12);
        for (int month = 1; month <= 12; month++) {
            months.add(buildMonth(businessId, YearMonth.of(year.getValue(), month)));
        }

        Total totals = Total.combine(months.stream().map(MonthlyReport::totals).toList());
        return new YearlyReport(year, months, totals);
    }

    private Total calculateTotals(List<Transaction> transactions, List<Expense> expenses, List<Payout> payouts) {
        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal tips = BigDecimal.ZERO;
        BigDecimal expenseTotal = BigDecimal.ZERO;
        BigDecimal payoutTotal = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            revenue = revenue.add(transaction.price());
            if (transaction.tip() != null) {
                tips = tips.add(transaction.tip());
            }
        }
        for (Expense expense : expenses) {
            expenseTotal = expenseTotal.add(expense.cost());
        }
        for (Payout payout : payouts) {
            payoutTotal = payoutTotal.add(payout.amount());
        }

        return new Total(revenue, tips, expenseTotal, payoutTotal);
    }

    private boolean isCompleteMonth(ReportRequest request) {
        if (request.startDate() == null || request.endDate() == null) {
            return false;
        }
        YearMonth month = YearMonth.from(request.startDate());
        return request.startDate().equals(month.atDay(1))
                && request.endDate().equals(month.atEndOfMonth());
    }

    private boolean isCompleteYear(ReportRequest request) {
        if (request.startDate() == null || request.endDate() == null) {
            return false;
        }
        Year year = Year.from(request.startDate());
        return request.startDate().equals(year.atDay(1))
                && request.endDate().equals(year.atMonth(12).atEndOfMonth());
    }
}
