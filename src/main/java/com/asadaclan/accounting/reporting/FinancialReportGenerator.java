package com.asadaclan.accounting.reporting;

import java.io.File;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.asadaclan.accounting.entities.Expense;
import com.asadaclan.accounting.entities.Payout;
import com.asadaclan.accounting.entities.Transaction;
import com.asadaclan.accounting.exceptions.FileUtilException;
import com.asadaclan.accounting.exceptions.PdfGenerationException;
import com.asadaclan.accounting.models.MonthlyReport;
import com.asadaclan.accounting.models.Total;
import com.asadaclan.accounting.models.YearlyReport;
import com.asadaclan.accounting.util.FileUtil;

@Service
public class FinancialReportGenerator {

    private final LatexPdfCompiler pdfCompiler;
    private final Resource monthlyTemplate;
    private final Resource yearlyTemplate;
    private final Resource monthSectionTemplate;
    private final Resource revenueTemplate;
    private final Resource expenseTemplate;
    private final Resource payoutTemplate;
    private final Resource totalsTemplate;

    public FinancialReportGenerator(LatexPdfCompiler pdfCompiler,
            @Value("${templates.locations.monthly}") Resource monthlyTemplate,
            @Value("${templates.locations.yearly}") Resource yearlyTemplate,
            @Value("${templates.locations.month-section}") Resource monthSectionTemplate,
            @Value("${templates.locations.revenue}") Resource revenueTemplate,
            @Value("${templates.locations.expenses}") Resource expenseTemplate,
            @Value("${templates.locations.payouts}") Resource payoutTemplate,
            @Value("${templates.locations.totals}") Resource totalsTemplate) {
        this.pdfCompiler = pdfCompiler;
        this.monthlyTemplate = monthlyTemplate;
        this.yearlyTemplate = yearlyTemplate;
        this.monthSectionTemplate = monthSectionTemplate;
        this.revenueTemplate = revenueTemplate;
        this.expenseTemplate = expenseTemplate;
        this.payoutTemplate = payoutTemplate;
        this.totalsTemplate = totalsTemplate;
    }

    public File generate(MonthlyReport report)
            throws FileUtilException, PdfGenerationException, InterruptedException {
        File rawLatexFile = populateMonthlyTemplate(report);
        try {
            String monthYear = report.period().format(DateTimeFormatter.ofPattern("MM_yyyy"));
            return pdfCompiler.compile(rawLatexFile, monthYear + "_MonthlyFinancialReport");
        } finally {
            FileUtil.deleteIfExists(rawLatexFile);
        }
    }

    public File generate(YearlyReport report)
            throws FileUtilException, PdfGenerationException, InterruptedException {
        File rawLatexFile = populateYearlyTemplate(report);
        try {
            return pdfCompiler.compile(rawLatexFile, report.year() + "_YearlyFinancialReport");
        } finally {
            FileUtil.deleteIfExists(rawLatexFile);
        }
    }

    public File populateMonthlyTemplate(MonthlyReport report) throws FileUtilException {
        return FileUtil.writeToFile(renderMonth(monthlyTemplate, report));
    }

    public File populateYearlyTemplate(YearlyReport report) throws FileUtilException {
        StringBuilder months = new StringBuilder();
        for (MonthlyReport month : report.months()) {
            months.append(renderMonth(monthSectionTemplate, month));
        }

        String reportContent = FileUtil.readString(yearlyTemplate)
                .replace("%%year%%", report.year().toString())
                .replace("%%totals%%", renderTotals(report.totals()))
                .replace("%%months%%", months.toString());
        return FileUtil.writeToFile(reportContent);
    }

    private String renderMonth(Resource template, MonthlyReport report) throws FileUtilException {
        String revenueRow = FileUtil.readString(revenueTemplate);
        StringBuilder revenues = new StringBuilder();
        for (Transaction transaction : report.transactions()) {
            revenues.append(String.format(Locale.ROOT, revenueRow,
                    escapeLatex(displayText(transaction.name())), transaction.price(), zeroIfNull(transaction.tip())));
        }
        if (revenues.isEmpty()) {
            revenues.append("\\multicolumn{3}{c}{No revenue transactions} \\\\\n");
        }

        String expenseRow = FileUtil.readString(expenseTemplate);
        StringBuilder expenses = new StringBuilder();
        for (Expense expense : report.expenses()) {
            expenses.append(String.format(Locale.ROOT, expenseRow,
                    escapeLatex(displayText(expense.reason())), expense.cost()));
        }
        if (expenses.isEmpty()) {
            expenses.append("\\multicolumn{2}{c}{No expenses} \\\\\n");
        }

        String payoutRow = FileUtil.readString(payoutTemplate);
        StringBuilder payouts = new StringBuilder();
        for (Payout payout : report.payouts()) {
            payouts.append(String.format(Locale.ROOT, payoutRow, payout.amount()));
        }
        if (payouts.isEmpty()) {
            payouts.append("No payouts \\\\\n");
        }

        String period = report.period().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + " " + report.period().getYear();
        return FileUtil.readString(template)
                .replace("%%period%%", period)
                .replace("%%totals%%", renderTotals(report.totals()))
                .replace("%%income%%", revenues.toString())
                .replace("%%expense%%", expenses.toString())
                .replace("%%payout%%", payouts.toString());
    }

    private String renderTotals(Total totals) throws FileUtilException {
        return String.format(Locale.ROOT, FileUtil.readString(totalsTemplate),
                totals.getRevenue(), totals.getTip(), totals.getExpenses(), totals.getPayouts(),
                totals.getEstimatedTaxes(), totals.getTakeHome());
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String displayText(String value) {
        return value == null || value.isBlank() ? "Unspecified" : value;
    }

    private String escapeLatex(String value) {
        StringBuilder escaped = new StringBuilder();
        for (char character : value.toCharArray()) {
            switch (character) {
                case '\\' -> escaped.append("\\textbackslash{}");
                case '&' -> escaped.append("\\&");
                case '%' -> escaped.append("\\%");
                case '$' -> escaped.append("\\$");
                case '#' -> escaped.append("\\#");
                case '_' -> escaped.append("\\_");
                case '{' -> escaped.append("\\{");
                case '}' -> escaped.append("\\}");
                case '~' -> escaped.append("\\textasciitilde{}");
                case '^' -> escaped.append("\\textasciicircum{}");
                default -> escaped.append(character);
            }
        }
        return escaped.toString();
    }
}
