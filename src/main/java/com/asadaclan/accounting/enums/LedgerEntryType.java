package com.asadaclan.accounting.enums;

import java.util.Locale;

public enum LedgerEntryType {
    TRANSACTION,
    EXPENSE,
    PAYOUT;

    public static LedgerEntryType from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("entry type must not be null");
        }

        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "transaction", "tattoo", "jewelry", "jewerly" -> TRANSACTION;
            case "expense" -> EXPENSE;
            case "payout" -> PAYOUT;
            default -> throw new IllegalArgumentException("Unknown ledger entry type: " + value);
        };
    }
}
