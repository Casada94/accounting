package com.asadaclan.accounting.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Total{
    private final float revenue;
    private final float tip;
    private final float expenses;
    private final float grossIncome;
    private final float estimatedTaxes;
    private final float takeHome;

    public Total(float revenue, float tip, float expenses) {
        this.revenue = revenue;
        this.tip = tip;
        this.expenses = expenses;

        // Expenses are negative, and tips are tax exempt.
        this.grossIncome = revenue + expenses;

        this.estimatedTaxes = (float) (this.grossIncome * 0.25);
        this.takeHome = grossIncome - estimatedTaxes + tip;
    }
}
