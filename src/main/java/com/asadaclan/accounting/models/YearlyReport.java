package com.asadaclan.accounting.models;

import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

import com.asadaclan.accounting.entities.Expense;
import com.asadaclan.accounting.entities.Payout;
import com.asadaclan.accounting.entities.Transaction;

public record YearlyReport(Year year, List<MonthlyReport> months, Total totals) {

    public YearlyReport {
        Objects.requireNonNull(year, "year must not be null");
        months = List.copyOf(months);
        Objects.requireNonNull(totals, "totals must not be null");

        if (months.size() != Month.values().length) {
            throw new IllegalArgumentException("A yearly report must contain exactly 12 months");
        }

        for (int index = 0; index < months.size(); index++) {
            YearMonth expectedPeriod = YearMonth.of(year.getValue(), index + 1);
            if (!months.get(index).period().equals(expectedPeriod)) {
                throw new IllegalArgumentException("Yearly report months must be ordered January through December");
            }
        }
    }

    public List<List<Transaction>> transactions() {
        return months.stream().map(MonthlyReport::transactions).toList();
    }

    public List<List<Expense>> expenses() {
        return months.stream().map(MonthlyReport::expenses).toList();
    }

    public List<List<Payout>> payouts() {
        return months.stream().map(MonthlyReport::payouts).toList();
    }
}
