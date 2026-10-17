package com.asadaclan.accounting.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import lombok.Getter;

@Getter
public final class Total {
    private static final BigDecimal TAX_RATE = new BigDecimal("0.25");
    private static final int MONEY_SCALE = 2;

    private final BigDecimal revenue;
    private final BigDecimal tip;
    private final BigDecimal expenses;
    private final BigDecimal payouts;
    private final BigDecimal grossIncome;
    private final BigDecimal estimatedTaxes;
    private final BigDecimal takeHome;

    public Total(BigDecimal revenue, BigDecimal tip, BigDecimal expenses, BigDecimal payouts) {
        this.revenue = money(revenue);
        this.tip = money(tip);
        this.expenses = money(expenses);
        this.payouts = money(payouts);
        this.grossIncome = money(this.revenue.subtract(this.expenses));
        this.estimatedTaxes = money(this.grossIncome.multiply(TAX_RATE));
        this.takeHome = money(this.grossIncome.subtract(this.estimatedTaxes).add(this.tip));
    }

    public static Total zero() {
        return new Total(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public static Total combine(List<Total> totals) {
        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal tip = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;
        BigDecimal payouts = BigDecimal.ZERO;

        for (Total total : totals) {
            revenue = revenue.add(total.revenue);
            tip = tip.add(total.tip);
            expenses = expenses.add(total.expenses);
            payouts = payouts.add(total.payouts);
        }

        return new Total(revenue, tip, expenses, payouts);
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
