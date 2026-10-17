package com.asadaclan.accounting.enums;

import java.util.Locale;

public enum MessageAction {
    Transaction,
    Payout,
    Expense,
    // Query,
    Report,
    Undo,
    Help,
    Default;

    public static MessageAction from(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("action must not be blank");
        }
        return switch (token.trim().toLowerCase(Locale.ROOT)) {
            case "tattoo", "jewelry", "jewerly", "transaction" -> Transaction;
            case "payout" -> Payout;
            case "expense" -> Expense;
            case "report" -> Report;
            case "undo" -> Undo;
            case "help" -> Help;
            case "default" -> Default;
            default -> throw new IllegalArgumentException("Unknown action: " + token.trim());
        };
    }
}
