package com.asadaclan.accounting.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.asadaclan.accounting.entities.Help;
import com.asadaclan.accounting.enums.MessageAction;

class HelpServiceTests {
    private static final String TRANSACTION_MESSAGE = "Tattoo/Jewelry: tattoo, price, tip, name?, date?";
    private static final String PAYOUT_MESSAGE = "Payout: payout, amount, date?";
    private static final String EXPENSE_MESSAGE = "Expense: expense, cost, reason?, date?";
    // private static final String QUERY_MESSAGE = "Query: query, queryType, args?";
    private static final String REPORT_MESSAGE =
            "Report: report, month, month?, year? OR report, year, year?";
    private static final String UNDO_MESSAGE = "Undo: undo, transactionId";

    private final HelpService helpService = new HelpService(
            TRANSACTION_MESSAGE,
            PAYOUT_MESSAGE,
            EXPENSE_MESSAGE,
            // QUERY_MESSAGE,
            REPORT_MESSAGE,
            UNDO_MESSAGE);

    @Test
    void returnsAllHelpMessagesWhenNoActionIsProvided() {
        assertThat(helpService.processHelp(new Help()))
                .contains(TRANSACTION_MESSAGE)
                .contains(PAYOUT_MESSAGE)
                .contains(EXPENSE_MESSAGE)
                // .contains(QUERY_MESSAGE)
                .contains(REPORT_MESSAGE)
                .contains(UNDO_MESSAGE);
    }

    @Test
    void returnsOnlyTheRequestedActionHelpMessage() {
        assertThat(helpService.processHelp(new Help(Optional.of(MessageAction.Expense))))
                .isEqualTo(EXPENSE_MESSAGE);
    }

    @Test
    void rejectsNullRequest() {
        assertThatThrownBy(() -> helpService.processHelp(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Help request must not be null");
    }
}
