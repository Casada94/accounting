package com.asadaclan.accounting.controllers;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.asadaclan.accounting.reporting.FinancialReportGenerator;
import com.asadaclan.accounting.service.ProcessExecutionService;
import com.asadaclan.accounting.service.QueryService;

import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

import com.asadaclan.accounting.enums.MessageAction;
import com.asadaclan.accounting.enums.MessageComponents;
import com.asadaclan.accounting.exceptions.FileUtilException;
import com.asadaclan.accounting.exceptions.ParseMessageException;
import com.asadaclan.accounting.exceptions.ProcessExecutionServiceException;
import com.asadaclan.accounting.exceptions.ReportException;
import com.asadaclan.accounting.models.ReceivedMessage;
import com.asadaclan.accounting.models.Transaction;

@Controller
public class SmsController {

    private final ProcessExecutionService processExecutionService;

    private final FinancialReportGenerator financialReportGenerator;
    private final QueryService queryService;
    private final List<DateTimeFormatter> formatters;

    public SmsController(FinancialReportGenerator financialReportGenerator, QueryService queryService,
            ProcessExecutionService processExecutionService,
            @Value("${spring.message.date.formats:MM/dd/yyyy,yyyy-MM-dd}") List<String> dateFormats) {
        this.financialReportGenerator = financialReportGenerator;
        this.queryService = queryService;
        this.processExecutionService = processExecutionService;
        this.formatters = dateFormats.stream().map(format -> DateTimeFormatter.ofPattern(format)).toList();
    }

    @PostMapping(value = "/sms", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> receiveSms(@RequestParam("Body") String body)
            throws ParseMessageException, ReportException, FileUtilException, ProcessExecutionServiceException,
            InterruptedException, MessagingException {
        String response = "";
        ReceivedMessage message = parseMessage(body);

        switch (message.messageAction()) {
            case MessageAction.Transaction:
                Transaction transaction = validateTransaction(message);
                // String transId = dao.save(transaction);
                String transId = "tempId";
                response = "transactionId: " + transId;
                break;
            case MessageAction.Expense:
                Transaction expense = validateExpense(message);
                // String transId = dao.save(transaction);
                String expenseId = "tempId";
                response = "transactionId: " + expenseId;
                break;
            case MessageAction.Query:
                // Query query = validateQuery(message);
                response = queryService.answer(message);
                break;
            case MessageAction.MonthlyReport:
                LocalDate month = validateReportRequest(message);
                financialReportGenerator.monthlyFinancialReport(month);
                response = "Please check your email for your financial report.";
                break;
            case MessageAction.YearlyReport:
                LocalDate year = validateReportRequest(message);
                financialReportGenerator.yearlyFinancialReport(year);
                response = "Please check your email for your financial report.";
                break;

            default:
                break;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(createTwimlResponse(response));
    }

    private String createTwimlResponse(String message) {
        String escapedMessage = HtmlUtils.htmlEscape(message, StandardCharsets.UTF_8.name());
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Response><Message>" + escapedMessage + "</Message></Response>";
    }

    private ReceivedMessage parseMessage(String rawMessage) throws ParseMessageException {
        try {
            List<String> splittedMessage = Arrays.asList(rawMessage.split(","));

            if (splittedMessage.size() < 3) {
                // message should contain atleast 1) messageAction, 2) item(s) , 3) dollar
                // amount
                throw new ParseMessageException("Not enought information");
            }

            MessageAction action = MessageAction.Default;
            Optional<String> transactionId = Optional.empty();
            Optional<LocalDate> date = Optional.empty();
            String items = "";
            Float transactionAmount = 0.0f;
            Optional<Float> tip = Optional.empty();
            // Optional<Image> image;
            MessageComponents messageComponents = MessageComponents.MessageAction;
            int position = 0;
            while (position < splittedMessage.size()) {
                switch (messageComponents) {
                    case MessageComponents.MessageAction:
                        action = MessageAction.valueOf(splittedMessage.get(position));
                        position++;
                        messageComponents = MessageComponents.TransactionId;
                        break;
                    case MessageComponents.TransactionId:
                        if (splittedMessage.get(position).startsWith("trx")) {
                            transactionId = Optional.of(splittedMessage.get(position));
                            position++;
                        }
                        messageComponents = MessageComponents.Date;
                        break;
                    case MessageComponents.Date:
                        date = parseDate(splittedMessage.get(position));
                        if (date.isPresent()) {
                            position++;
                        }
                        messageComponents = MessageComponents.Items;
                        break;
                    case MessageComponents.Items:
                        items = splittedMessage.get(position);
                        position++;
                        messageComponents = MessageComponents.TransactionAmount;
                        break;
                    case MessageComponents.TransactionAmount:
                        transactionAmount = Float.valueOf(splittedMessage.get(position));
                        position++;
                        messageComponents = MessageComponents.Tip;
                        break;
                    case MessageComponents.Tip:
                        try {
                            tip = Optional.of(Float.valueOf(splittedMessage.get(position)));
                            position++;
                        } catch (NumberFormatException ignore) {
                        }
                        messageComponents = MessageComponents.Image;
                        break;
                    case MessageComponents.Image:
                        System.out.println("NOT IMPLEMENTED");
                    default:
                        throw new RuntimeException("how did you manage to do this??");
                }
            }
            return new ReceivedMessage(action, transactionId, date, items, transactionAmount, tip);
        } catch (Exception exception) {
            throw new ParseMessageException("Unable to parse message.");
        }
    }

    private Optional<LocalDate> parseDate(String rawDate) {
        for (DateTimeFormatter formatter : formatters) {
            try {
                return Optional.of(LocalDate.parse(rawDate, formatter));
            } catch (DateTimeParseException ignore) {
            }
        }
        return Optional.empty();
    }

    public Transaction validateTransaction(ReceivedMessage message) {
        return new Transaction(message.items(), message.transactionAmount(), message.tip());
    }

    public Transaction validateExpense(ReceivedMessage message) {
        if (message.transactionAmount() > 0) {
            return new Transaction(message.items(), message.transactionAmount() * -1, message.tip());
        }
        return new Transaction(message.items(), message.transactionAmount(), message.tip());

    }

    // public Query validateQuery(ReceivedMessage message) {

    // }

    public LocalDate validateReportRequest(ReceivedMessage message) throws ReportException {
        try {
            return message.date().orElseThrow();
        } catch (NoSuchElementException noSuchElementException) {
            throw new ReportException("Can't generate a report without a date");
        }
    }
}
