package com.asadaclan.accounting.services;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.asadaclan.accounting.entities.Help;
import com.asadaclan.accounting.enums.MessageAction;

@Service
public class HelpService {
    private final Map<MessageAction, String> helpMessages;

    public HelpService(
            @Value("${help.messages.transaction}") String transactionMessage,
            @Value("${help.messages.payout}") String payoutMessage,
            @Value("${help.messages.expense}") String expenseMessage,
            // @Value("${help.messages.query}") String queryMessage,
            @Value("${help.messages.report}") String reportMessage,
            @Value("${help.messages.undo}") String undoMessage) {
        Map<MessageAction, String> messages = new EnumMap<>(MessageAction.class);
        messages.put(MessageAction.Transaction, transactionMessage);
        messages.put(MessageAction.Payout, payoutMessage);
        messages.put(MessageAction.Expense, expenseMessage);
        // messages.put(MessageAction.Query, queryMessage);
        messages.put(MessageAction.Report, reportMessage);
        messages.put(MessageAction.Undo, undoMessage);
        this.helpMessages = Collections.unmodifiableMap(messages);
    }

    public String processHelp(Help help) {
        if (help == null) {
            throw new IllegalArgumentException("Help request must not be null");
        }

        return help.action()
                .map(helpMessages::get)
                .orElseGet(() -> String.join("\n", helpMessages.values()));
    }
}
