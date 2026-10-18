package com.asadaclan.accounting.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.asadaclan.accounting.entities.Expense;
import com.asadaclan.accounting.entities.Payout;
import com.asadaclan.accounting.entities.ReportRequest;
import com.asadaclan.accounting.entities.Transaction;
import com.asadaclan.accounting.entities.Undo;
import com.asadaclan.accounting.enums.LedgerEntryType;
import com.asadaclan.accounting.util.DateParser;

class ParserBusinessIdTests {
    private static final String BUSINESS_ID = "+15551234567";
    private static final DateParser DATE_PARSER = new DateParser(
            Clock.fixed(Instant.parse("2026-10-17T12:00:00Z"), ZoneOffset.UTC));

    @Test
    void transactionIncludesTheAuthorizedBusinessId() {
        Transaction transaction = new TransactionParser(DATE_PARSER)
                .parse(List.of("tattoo", "250", "50"), BUSINESS_ID);

        assertEquals(BUSINESS_ID, transaction.businessId());
    }

    @Test
    void expenseIncludesTheAuthorizedBusinessId() {
        Expense expense = new ExpenseParser(DATE_PARSER)
                .parse(List.of("expense", "25"), BUSINESS_ID);

        assertEquals(BUSINESS_ID, expense.businessId());
    }

    @Test
    void payoutIncludesTheAuthorizedBusinessId() {
        Payout payout = new PayoutParser(DATE_PARSER)
                .parse(List.of("payout", "500"), BUSINESS_ID);

        assertEquals(BUSINESS_ID, payout.businessId());
    }

    @Test
    void reportIncludesTheAuthorizedBusinessId() {
        ReportRequest report = new ReportParser(DATE_PARSER)
                .parse(List.of("report", "year", "2026"), BUSINESS_ID);

        assertEquals(BUSINESS_ID, report.businessId());
    }

    @Test
    void undoIncludesTheAuthorizedBusinessId() {
        Undo undo = new UndoParser().parse(List.of(
                "undo", "expense", "3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e47"), BUSINESS_ID);

        assertEquals(BUSINESS_ID, undo.businessId());
        assertEquals(LedgerEntryType.EXPENSE, undo.entryType());
    }
}
