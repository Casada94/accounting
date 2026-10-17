package com.asadaclan.accounting.controllers;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asadaclan.accounting.entities.Expense;
import com.asadaclan.accounting.entities.Help;
import com.asadaclan.accounting.entities.Payout;
import com.asadaclan.accounting.entities.Report;
import com.asadaclan.accounting.entities.Transaction;
import com.asadaclan.accounting.entities.Undo;
import com.asadaclan.accounting.enums.MessageAction;
import com.asadaclan.accounting.models.TwimlResponse;
import com.asadaclan.accounting.services.MessageDelegationService;
import com.asadaclan.accounting.services.ParserDelegationService;

@RestController
public class SmsController {

    private final MessageDelegationService messageDelegationService;
    private final ParserDelegationService parserDelegationService;

    public SmsController(MessageDelegationService messageDelegationService,
            ParserDelegationService parserDelegationService) {
        this.messageDelegationService = messageDelegationService;
        this.parserDelegationService = parserDelegationService;
    }

    @PostMapping(value = "/sms", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public TwimlResponse receiveSms(@RequestParam("Body") String body) {
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
                        MessageAction.Transaction);
                yield messageDelegationService.delegate(transaction);
            }
            case Payout -> {
                Payout payout = (Payout) parserDelegationService.delegate(messageParts, MessageAction.Payout);
                yield messageDelegationService.delegate(payout);
            }
            case Expense -> {
                Expense expense = (Expense) parserDelegationService.delegate(messageParts, MessageAction.Expense);
                yield messageDelegationService.delegate(expense);
            }
            case Report -> {
                Report report = (Report) parserDelegationService.delegate(messageParts, MessageAction.Report);
                messageDelegationService.delegate(report);
                yield "Please check your email for your financial report.";
            }
            case Undo -> {
                Undo undo = (Undo) parserDelegationService.delegate(messageParts, MessageAction.Undo);
                yield messageDelegationService.delegate(undo);
            }
            case Help -> {
                Help help = (Help) parserDelegationService.delegate(messageParts, MessageAction.Help);
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
