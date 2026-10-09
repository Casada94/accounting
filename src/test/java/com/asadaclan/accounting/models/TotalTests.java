package com.asadaclan.accounting.models;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TotalTests {

    @Test
    void excludesTipsFromTaxesButIncludesThemInTakeHomeIncome() {
        Total total = new Total(100.0f, 10.0f, -20.0f);

        assertThat(total.getGrossIncome()).isEqualTo(80.0f);
        assertThat(total.getEstimatedTaxes()).isEqualTo(20.0f);
        assertThat(total.getTakeHome()).isEqualTo(70.0f);
    }
}
