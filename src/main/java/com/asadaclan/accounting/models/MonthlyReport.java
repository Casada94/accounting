package com.asadaclan.accounting.models;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

import com.asadaclan.accounting.entities.Expense;
import com.asadaclan.accounting.entities.Payout;
import com.asadaclan.accounting.entities.Transaction;

public record MonthlyReport(
        YearMonth period,
        List<Transaction> transactions,
        List<Expense> expenses,
        List<Payout> payouts,
        Total totals) {

    public MonthlyReport {
        Objects.requireNonNull(period, "period must not be null");
        transactions = List.copyOf(transactions);
        expenses = List.copyOf(expenses);
        payouts = List.copyOf(payouts);
        Objects.requireNonNull(totals, "totals must not be null");
    }
}
