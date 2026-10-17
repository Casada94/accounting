package com.asadaclan.accounting.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MessageActionTests {

    @Test
    void mapsTattooAndJewelryToTransaction() {
        assertThat(MessageAction.from("tattoo")).isEqualTo(MessageAction.Transaction);
        assertThat(MessageAction.from("Tattoo")).isEqualTo(MessageAction.Transaction);
        assertThat(MessageAction.from("TATTOO")).isEqualTo(MessageAction.Transaction);
        assertThat(MessageAction.from("jewelry")).isEqualTo(MessageAction.Transaction);
        assertThat(MessageAction.from("jewerly")).isEqualTo(MessageAction.Transaction);
        assertThat(MessageAction.from("JEWERLY")).isEqualTo(MessageAction.Transaction);
        assertThat(MessageAction.from("transaction")).isEqualTo(MessageAction.Transaction);
        assertThat(MessageAction.from("Transaction")).isEqualTo(MessageAction.Transaction);
    }

    @Test
    void mapsRemainingVerbsRegardlessOfCase() {
        assertThat(MessageAction.from("expense")).isEqualTo(MessageAction.Expense);
        assertThat(MessageAction.from("EXPENSE")).isEqualTo(MessageAction.Expense);
        assertThat(MessageAction.from("payout")).isEqualTo(MessageAction.Payout);
        assertThat(MessageAction.from("Payout")).isEqualTo(MessageAction.Payout);
        assertThat(MessageAction.from("report")).isEqualTo(MessageAction.Report);
        assertThat(MessageAction.from("Report")).isEqualTo(MessageAction.Report);
        assertThat(MessageAction.from("undo")).isEqualTo(MessageAction.Undo);
        assertThat(MessageAction.from("Undo")).isEqualTo(MessageAction.Undo);
        assertThat(MessageAction.from("help")).isEqualTo(MessageAction.Help);
        assertThat(MessageAction.from("Help")).isEqualTo(MessageAction.Help);
        assertThat(MessageAction.from(" default ")).isEqualTo(MessageAction.Default);
    }

    @Test
    void rejectsBlankAndUnknownActions() {
        assertThatThrownBy(() -> MessageAction.from(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MessageAction.from("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MessageAction.from("   ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MessageAction.from("unknown")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MessageAction.from("query")).isInstanceOf(IllegalArgumentException.class);
    }
}
