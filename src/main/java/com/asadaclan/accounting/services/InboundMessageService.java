package com.asadaclan.accounting.services;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.asadaclan.accounting.entities.Business;
import com.asadaclan.accounting.entities.Expense;
import com.asadaclan.accounting.entities.Help;
import com.asadaclan.accounting.entities.Payout;
import com.asadaclan.accounting.entities.ReportRequest;
import com.asadaclan.accounting.entities.Transaction;
import com.asadaclan.accounting.entities.Undo;
import com.asadaclan.accounting.enums.MessageAction;

@Service
public class InboundMessageService {

    private final MessageDelegationService messageDelegationService;
    private final ParserDelegationService parserDelegationService;
    private final BusinessAuthorizationService businessAuthorizationService;
    private final ReportService reportService;

    public InboundMessageService(MessageDelegationService messageDelegationService,
            ParserDelegationService parserDelegationService,
            BusinessAuthorizationService businessAuthorizationService,
            ReportService reportService) {
        this.messageDelegationService = messageDelegationService;
        this.parserDelegationService = parserDelegationService;
        this.businessAuthorizationService = businessAuthorizationService;
        this.reportService = reportService;
    }

    public String process(String sender, String body) {
        Business business = businessAuthorizationService.requireBusiness(sender);
        String businessId = business.phoneNumber();

        if (body == null || body.isBlank()) {
            return "Message must not be blank";
        }
        List<String> messageParts = Arrays.asList(body.split(",", -1));
        if (messageParts.isEmpty() || isBlank(messageParts.getFirst())) {
            return "Message must not be blank";
        }

        MessageAction action = MessageAction.from(messageParts.getFirst());
        return switch (action) {
            case Transaction -> {
                Transaction transaction = (Transaction) parserDelegationService.delegate(
                        messageParts, MessageAction.Transaction, businessId);
                yield messageDelegationService.delegate(transaction);
            }
            case Payout -> {
                Payout payout = (Payout) parserDelegationService.delegate(
                        messageParts, MessageAction.Payout, businessId);
                yield messageDelegationService.delegate(payout);
            }
            case Expense -> {
                Expense expense = (Expense) parserDelegationService.delegate(
                        messageParts, MessageAction.Expense, businessId);
                yield messageDelegationService.delegate(expense);
            }
            case Report -> {
                ReportRequest report = (ReportRequest) parserDelegationService.delegate(
                        messageParts, MessageAction.Report, businessId);
                reportService.generateReport(business, report);
                yield "Please check your email for your financial report.";
            }
            case Undo -> {
                Undo undo = (Undo) parserDelegationService.delegate(
                        messageParts, MessageAction.Undo, businessId);
                yield messageDelegationService.delegate(undo);
            }
            case Help -> {
                Help help = (Help) parserDelegationService.delegate(
                        messageParts, MessageAction.Help, businessId);
                yield messageDelegationService.delegate(help);
            }
            case Default -> throw new IllegalArgumentException(
                    "Unknown action: " + messageParts.getFirst().trim());
        };
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
