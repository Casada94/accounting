package com.asadaclan.accounting.reporting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.asadaclan.accounting.entities.Expense;
import com.asadaclan.accounting.entities.Payout;
import com.asadaclan.accounting.entities.Transaction;
import com.asadaclan.accounting.models.MonthlyReport;
import com.asadaclan.accounting.models.Total;
import com.asadaclan.accounting.models.YearlyReport;
import com.asadaclan.accounting.util.FileUtil;

class FinancialReportGeneratorTests {

    @TempDir
    Path tempDir;

    @Test
    void populateMonthlyTemplateReplacesEveryReportSection() throws Exception {
        Resource reportTemplate = writeTemplate("report.template",
                "%%period%%\nIncome:\n%%income%%Expenses:\n%%expense%%Payouts:\n%%payout%%Totals:\n%%totals%%");
        FinancialReportGenerator generator = generator(reportTemplate, reportTemplate, reportTemplate);
        Transaction revenue = new Transaction(
                new BigDecimal("100.00"), new BigDecimal("10.00"),
                "consulting & support", LocalDate.of(2026, 10, 14));
        Expense expense = new Expense(
                new BigDecimal("20.00"), "ink_kit", LocalDate.of(2026, 10, 14));
        Payout payout = new Payout(new BigDecimal("30.00"), LocalDate.of(2026, 10, 14));
        Total totals = new Total(
                new BigDecimal("100.00"), new BigDecimal("10.00"),
                new BigDecimal("20.00"), new BigDecimal("30.00"));
        MonthlyReport report = new MonthlyReport(
                YearMonth.of(2026, 10), List.of(revenue), List.of(expense), List.of(payout), totals);

        File populatedReport = generator.populateMonthlyTemplate(report);

        assertThat(Files.readString(populatedReport.toPath()))
                .contains("October 2026")
                .contains("Revenue: consulting \\& support, 100.00, 10.00")
                .contains("Expense: ink\\_kit, 20.00")
                .contains("Payout: 30.00")
                .contains("100.00|10.00|20.00|30.00|20.00|70.00")
                .doesNotContain("%%period%%", "%%income%%", "%%expense%%", "%%payout%%", "%%totals%%");
    }

    @Test
    void yearlyTemplateRendersAllTwelveMonthsInOrder() throws Exception {
        Resource monthlyTemplate = writeTemplate("monthly.template", "%%period%%");
        Resource yearlyTemplate = writeTemplate("yearly.template", "Year %%year%%\n%%totals%%\n%%months%%");
        Resource monthTemplate = writeTemplate("month.template", "Month %%period%%\n%%totals%%\n");
        FinancialReportGenerator generator = generator(monthlyTemplate, yearlyTemplate, monthTemplate);
        List<MonthlyReport> months = java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(month -> new MonthlyReport(
                        YearMonth.of(2026, month), List.of(), List.of(), List.of(), Total.zero()))
                .toList();
        YearlyReport report = new YearlyReport(Year.of(2026), months, Total.zero());

        String contents = Files.readString(generator.populateYearlyTemplate(report).toPath());

        assertThat(contents).contains("Year 2026", "Month January 2026", "Month December 2026");
        assertThat(contents.indexOf("Month January 2026"))
                .isLessThan(contents.indexOf("Month December 2026"));
        assertThat(contents).doesNotContain("%%year%%", "%%months%%", "%%totals%%");
    }

    @Test
    void readsReportTemplatesFromClasspath() throws Exception {
        Resource template = new ClassPathResource("templates/monthly-pdf.latex.template");

        assertThat(FileUtil.readString(template))
                .contains("Monthly Financial Report", "%%totals%%", "%%income%%", "%%expense%%", "%%payout%%");
    }

    private FinancialReportGenerator generator(
            Resource monthlyTemplate, Resource yearlyTemplate, Resource monthSectionTemplate) throws Exception {
        Resource revenueTemplate = writeTemplate("revenue.template", "Revenue: %s, %.2f, %.2f\n");
        Resource expenseTemplate = writeTemplate("expense.template", "Expense: %s, %.2f\n");
        Resource payoutTemplate = writeTemplate("payout.template", "Payout: %.2f\n");
        Resource totalsTemplate = writeTemplate("totals.template", "%.2f|%.2f|%.2f|%.2f|%.2f|%.2f");
        return new FinancialReportGenerator(
                mock(LatexPdfCompiler.class), monthlyTemplate, yearlyTemplate, monthSectionTemplate,
                revenueTemplate, expenseTemplate, payoutTemplate, totalsTemplate);
    }

    private Resource writeTemplate(String fileName, String contents) throws Exception {
        Path path = tempDir.resolve(fileName);
        Files.writeString(path, contents);
        return new FileSystemResource(path);
    }
}
