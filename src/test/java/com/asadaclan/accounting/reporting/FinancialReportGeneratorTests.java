package com.asadaclan.accounting.reporting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.asadaclan.accounting.models.Total;
import com.asadaclan.accounting.entities.Transaction;
import com.asadaclan.accounting.producers.EmailProducer;
import com.asadaclan.accounting.service.ProcessExecutionService;
import com.asadaclan.accounting.util.FileUtil;

class FinancialReportGeneratorTests {

    @TempDir
    Path tempDir;

    @Test
    void populateTemplateReplacesEveryReportSection() throws Exception {
        Resource reportTemplate = writeTemplate("report.template",
                "Income:\n%%income%%Expenses:\n%%expense%%Totals:\n%%totals%%");
        Resource revenueTemplate = writeTemplate("revenue.template", "Revenue: %s, %.2f, %s\n");
        Resource expenseTemplate = writeTemplate("expense.template", "Expense: %s, %.2f\n");
        Resource totalsTemplate = writeTemplate("totals.template", "%.2f|%.2f|%.2f|%.2f|%.2f");

        FinancialReportGenerator generator = new FinancialReportGenerator(
                mock(ProcessExecutionService.class),
                mock(EmailProducer.class),
                reportTemplate,
                reportTemplate,
                revenueTemplate,
                expenseTemplate,
                totalsTemplate);

        Transaction revenue = new Transaction(
                new BigDecimal("100.00"), new BigDecimal("10.00"), "consulting & support", LocalDate.of(2026, 10, 14));
        Transaction expense = new Transaction(
                new BigDecimal("-20.00"), BigDecimal.ZERO, "supplies", LocalDate.of(2026, 10, 14));
        Total totals = generator.processData(List.of(revenue), List.of(expense));

        File populatedReport = generator.populateTemplate(
                reportTemplate, List.of(revenue), List.of(expense), totals);

        assertThat(Files.readString(populatedReport.toPath()))
                .contains("Revenue: consulting \\& support, 100.00, 10.0")
                .contains("Expense: supplies, -20.00")
                .contains("100.00|10.00|-20.00|20.00|70.00")
                .doesNotContain("%%income%%", "%%expense%%", "%%totals%%");
    }

    @Test
    void readsReportTemplatesFromClasspath() throws Exception {
        Resource template = new ClassPathResource("templates/monthly-pdf.latex.template");

        assertThat(FileUtil.readString(template))
                .contains("Monthly Financial Report", "%%totals%%", "%%income%%", "%%expense%%");
    }

    private Resource writeTemplate(String fileName, String contents) throws Exception {
        Path path = tempDir.resolve(fileName);
        Files.writeString(path, contents);
        return new FileSystemResource(path);
    }
}
