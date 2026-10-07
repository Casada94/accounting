package com.asadaclan.accounting.reporting;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileUrlResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.asadaclan.accounting.exceptions.FileUtilException;
import com.asadaclan.accounting.exceptions.ProcessExecutionServiceException;
import com.asadaclan.accounting.models.Total;
import com.asadaclan.accounting.models.Transaction;
import com.asadaclan.accounting.producers.EmailProducer;
import com.asadaclan.accounting.service.ProcessExecutionService;
import com.asadaclan.accounting.util.FileUtil;

import jakarta.mail.MessagingException;

@Service
public class FinancialReportGenerator {

    private ProcessExecutionService execService;
    private EmailProducer emailProducer;
    private File monthlyTemplateFile;
    private File yearlyTemplateFile;
    private File revenueTemplateFile;
    private File expenseTemplateFile;
    private File totalsTemplateFile;

    public FinancialReportGenerator(ProcessExecutionService execService, EmailProducer emailProducer,
            @Value("templates.locations.monthly") String monthlyTemplateLocation,
            @Value("templates.locations.yearly") String yearlyTemplateLocation,
            @Value("templates.locations.revenue") String revenueTemplateLocation,
            @Value("templates.locations.expenses") String expenseTemplateLocation,
            @Value("templates.locations.totals") String totalsTemplateLocation) {
        this.execService = execService;
        this.emailProducer = emailProducer;

        // try {
            this.monthlyTemplateFile = new File(monthlyTemplateLocation);
            this.yearlyTemplateFile = new File(yearlyTemplateLocation);
            this.revenueTemplateFile = new File(revenueTemplateLocation);
            this.expenseTemplateFile = new File(expenseTemplateLocation);
            this.totalsTemplateFile = new File(totalsTemplateLocation);
        // } catch (IOException ioException) {
            // log.error(ioException.getMessage());
        //     throw ioException;
        // }
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

        File rawLatexFile = populateTemplate(monthlyTemplateFile, income, expenses, totals);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM_yyyy");
        String monthYear = targetMonth.format(formatter);

        File monthlyPDF = execService.generatePDF(rawLatexFile, monthYear + "_MonthlyFinancialReport.pdf");

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

        File rawLatexFile = populateTemplate(yearlyTemplateFile, income, expenses, totals);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
        String year = targetYear.format(formatter);

        File yearlyPDF = execService.generatePDF(rawLatexFile, year + "_YearlyFinancialReport.pdf");

        emailProducer.sendMail(year + " Financial Report", "", yearlyPDF);
    }

    public Total processData(List<Transaction> transactions, List<Transaction> expenses) {
        float revenueTotal = 0;
        float tipTotal = 0;

        for (Transaction transaction : transactions) {
            revenueTotal += transaction.transactionAmount();
            tipTotal += transaction.tipAmount().orElse(0.0f);
        }

        float expenseTotal = 0;
        for (Transaction expense : expenses) {
            expenseTotal += expense.transactionAmount();
        }

        return new Total(revenueTotal, tipTotal, expenseTotal);
    }

    // not super sold on the layout of these methods

    public File populateTemplate(File template, List<Transaction> transactions, List<Transaction> expenses,
            Total totals) throws FileUtilException {

        String revenueTemplate = FileUtil.readString(revenueTemplateFile);
        StringBuilder incomeBuilder = new StringBuilder();
        for (Transaction transaction : transactions) {
            // incomeBuilder.append(String.format(revenueTemplate, transaction.items(), transaction.transactionAmount(),
            //         transaction.tipAmount(), transaction.image()));
            incomeBuilder.append(String.format(revenueTemplate, transaction.items(), transaction.transactionAmount(),
                    transaction.tipAmount()));
        }

        String expenseTemplate = FileUtil.readString(expenseTemplateFile);
        StringBuilder expenseBuilder = new StringBuilder();
        for (Transaction transaction : transactions) {
            // expenseBuilder.append(String.format(expenseTemplate, transaction.items(), transaction.transactionAmount(),
            //         transaction.tipAmount(), transaction.image()));
            expenseBuilder.append(String.format(expenseTemplate, transaction.items(), transaction.transactionAmount(),
                    transaction.tipAmount()));
        }

        String totalsTemplate = FileUtil.readString(totalsTemplateFile);
        String totalsString = String.format(totalsTemplate, totals.getRevenue(), totals.getTip(), totals.getExpenses(),
                totals.getEstimatedTaxes(), totals.getTakeHome());

        String templateContent = FileUtil.readString(template);

        String reportContent = templateContent.replace("%%income%%", incomeBuilder.toString());
        reportContent = templateContent.replace("%%expense%%", expenseBuilder.toString());
        reportContent = templateContent.replace("%%totals%%", totalsString);

        File generatedLatexFile = FileUtil.writeToFile(reportContent);

        return generatedLatexFile;
    }

}
