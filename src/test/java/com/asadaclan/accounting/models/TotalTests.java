package com.asadaclan.accounting.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class TotalTests {

    @Test
    void excludesTipsFromTaxesButIncludesThemInTakeHomeIncome() {
        Total total = new Total(
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                new BigDecimal("30.00"));

        assertThat(total.getGrossIncome()).isEqualByComparingTo("80.00");
        assertThat(total.getEstimatedTaxes()).isEqualByComparingTo("20.00");
        assertThat(total.getTakeHome()).isEqualByComparingTo("70.00");
        assertThat(total.getPayouts()).isEqualByComparingTo("30.00");
    }
}
