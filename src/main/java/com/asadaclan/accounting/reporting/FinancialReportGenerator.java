package com.asadaclan.accounting.reporting;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.asadaclan.accounting.exceptions.FileUtilException;
import com.asadaclan.accounting.exceptions.ProcessExecutionServiceException;
import com.asadaclan.accounting.models.Total;
import com.asadaclan.accounting.entities.Transaction;
import com.asadaclan.accounting.producers.EmailProducer;
import com.asadaclan.accounting.service.ProcessExecutionService;
import com.asadaclan.accounting.util.FileUtil;

import jakarta.mail.MessagingException;

@Service
public class FinancialReportGenerator {

    private ProcessExecutionService execService;
    private EmailProducer emailProducer;
    private Resource monthlyTemplate;
    private Resource yearlyTemplate;
    private Resource revenueTemplate;
    private Resource expenseTemplate;
    private Resource totalsTemplate;

    public FinancialReportGenerator(ProcessExecutionService execService, EmailProducer emailProducer,
            @Value("${templates.locations.monthly}") Resource monthlyTemplate,
            @Value("${templates.locations.yearly}") Resource yearlyTemplate,
            @Value("${templates.locations.revenue}") Resource revenueTemplate,
            @Value("${templates.locations.expenses}") Resource expenseTemplate,
            @Value("${templates.locations.totals}") Resource totalsTemplate) {
        this.execService = execService;
        this.emailProducer = emailProducer;
        this.monthlyTemplate = monthlyTemplate;
        this.yearlyTemplate = yearlyTemplate;
        this.revenueTemplate = revenueTemplate;
        this.expenseTemplate = expenseTemplate;
        this.totalsTemplate = totalsTemplate;
    }

    @Scheduled(cron = "0 0 1 1 * *")
    public void monthlyFinancialReportAutomatic()  throws FileUtilException, ProcessExecutionServiceException, InterruptedException, MessagingException {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        monthlyFinancialReport(yesterday);
    }

    public void monthlyFinancialReport(LocalDate targetMonth) throws FileUtilException, ProcessExecutionServiceException, InterruptedException, MessagingException  {
        // List<Transaction> income = dao.getTransactionDataForMonth();
        // List<Transaction> expenses = dao.getExpenseDataForMonth();
        List<Transaction> income = new ArrayList<>();
        List<Transaction> expenses = new ArrayList<>();

        Total totals = processData(income, expenses);

        File rawLatexFile = populateTemplate(monthlyTemplate, income, expenses, totals);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM_yyyy");
        String monthYear = targetMonth.format(formatter);

        File monthlyPDF = execService.generatePDF(rawLatexFile, monthYear + "_MonthlyFinancialReport");

        emailProducer.sendMail(monthYear + " Financial Report", "", monthlyPDF);
    }

    @Scheduled(cron = "0 0 1 1 1 *")
    public void yearlyFinancialReportAutomatic() throws FileUtilException, ProcessExecutionServiceException, InterruptedException, MessagingException {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        yearlyFinancialReport(yesterday);
    }

    public void yearlyFinancialReport(LocalDate targetYear) throws FileUtilException, ProcessExecutionServiceException, InterruptedException, MessagingException {
        // List<Transaction> income = dao.getTransactionDataForYear();
        // List<Transaction> expenses = dao.getExpenseDataForYear();
        List<Transaction> income = new ArrayList<>();
        List<Transaction> expenses = new ArrayList<>();

        Total totals = processData(income, expenses);

        File rawLatexFile = populateTemplate(yearlyTemplate, income, expenses, totals);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
        String year = targetYear.format(formatter);

        File yearlyPDF = execService.generatePDF(rawLatexFile, year + "_YearlyFinancialReport");

        emailProducer.sendMail(year + " Financial Report", "", yearlyPDF);
    }

    public Total processData(List<Transaction> transactions, List<Transaction> expenses) {
        float revenueTotal = 0;
        float tipTotal = 0;

        for (Transaction transaction : transactions) {
            revenueTotal += transaction.price().floatValue();
            tipTotal += transaction.tip().floatValue();
        }

        float expenseTotal = 0;
        for (Transaction expense : expenses) {
            expenseTotal += expense.price().floatValue();
        }

        return new Total(revenueTotal, tipTotal, expenseTotal);
    }

    // not super sold on the layout of these methods

    public File populateTemplate(Resource template, List<Transaction> transactions, List<Transaction> expenses,
            Total totals) throws FileUtilException {

        String revenueTemplate = FileUtil.readString(this.revenueTemplate);
        StringBuilder incomeBuilder = new StringBuilder();
        for (Transaction transaction : transactions) {
            incomeBuilder.append(String.format(revenueTemplate, escapeLatex(transaction.name()),
                    transaction.price(), transaction.tip()));
        }

        String expenseTemplate = FileUtil.readString(this.expenseTemplate);
        StringBuilder expenseBuilder = new StringBuilder();
        for (Transaction transaction : expenses) {
            expenseBuilder.append(String.format(expenseTemplate, escapeLatex(transaction.name()),
                    transaction.price()));
        }

        String totalsTemplate = FileUtil.readString(this.totalsTemplate);
        String totalsString = String.format(totalsTemplate, totals.getRevenue(), totals.getTip(), totals.getExpenses(),
                totals.getEstimatedTaxes(), totals.getTakeHome());

        String templateContent = FileUtil.readString(template);

        String reportContent = templateContent.replace("%%income%%", incomeBuilder.toString());
        reportContent = reportContent.replace("%%expense%%", expenseBuilder.toString());
        reportContent = reportContent.replace("%%totals%%", totalsString);

        File generatedLatexFile = FileUtil.writeToFile(reportContent);

        return generatedLatexFile;
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
