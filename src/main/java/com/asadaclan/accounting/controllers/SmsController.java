package com.asadaclan.accounting.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asadaclan.accounting.entities.Expense;
import com.asadaclan.accounting.entities.Help;
import com.asadaclan.accounting.entities.Payout;
import com.asadaclan.accounting.entities.ReportRequest;
import com.asadaclan.accounting.entities.Transaction;
import com.asadaclan.accounting.entities.Undo;
import com.asadaclan.accounting.enums.MessageAction;
import com.asadaclan.accounting.models.TwilioInboundMessage;
import com.asadaclan.accounting.models.TwimlResponse;
import com.asadaclan.accounting.services.BusinessAuthorizationService;
import com.asadaclan.accounting.services.MessageDelegationService;
import com.asadaclan.accounting.services.ParserDelegationService;

@RestController
public class SmsController {

    private final MessageDelegationService messageDelegationService;
    private final ParserDelegationService parserDelegationService;
    private final BusinessAuthorizationService businessAuthorizationService;

    public SmsController(MessageDelegationService messageDelegationService,
            ParserDelegationService parserDelegationService,
            BusinessAuthorizationService businessAuthorizationService) {
        this.messageDelegationService = messageDelegationService;
        this.parserDelegationService = parserDelegationService;
        this.businessAuthorizationService = businessAuthorizationService;
    }

    @PostMapping(value = "/sms", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public TwimlResponse receiveSms(@ModelAttribute TwilioInboundMessage message) {
        UUID businessId = businessAuthorizationService.requireBusinessId(message.from());

        String body = message.body();
        if (body == null || body.isBlank()) {
            return new TwimlResponse("Message must not be blank");
        }
        List<String> messageParts = Arrays.asList(body.split(",", -1));
        if (messageParts.isEmpty() || isBlank(messageParts.getFirst())) {
            return new TwimlResponse("Message must not be blank");
        }
        MessageAction action = determineAction(messageParts.getFirst());
        String response = switch (action) {
            case Transaction -> {
                Transaction transaction = (Transaction) parserDelegationService.delegate(messageParts,
                        MessageAction.Transaction, businessId);
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
                messageDelegationService.delegate(report);
                yield "Please check your email for your financial report.";
            }
            case Undo -> {
                Undo undo = (Undo) parserDelegationService.delegate(messageParts, MessageAction.Undo, businessId);
                yield messageDelegationService.delegate(undo);
            }
            case Help -> {
                Help help = (Help) parserDelegationService.delegate(messageParts, MessageAction.Help, businessId);
                yield messageDelegationService.delegate(help);
            }
            case Default -> throw new IllegalArgumentException("Unknown action: " + messageParts.getFirst().trim());
        };
        return new TwimlResponse(response);
    }

    private MessageAction determineAction(String firstElement) {
        return MessageAction.from(firstElement);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
