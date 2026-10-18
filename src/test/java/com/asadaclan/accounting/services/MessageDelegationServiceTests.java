package com.asadaclan.accounting.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asadaclan.accounting.entities.Expense;
import com.asadaclan.accounting.entities.Help;
import com.asadaclan.accounting.entities.Payout;
import com.asadaclan.accounting.entities.Transaction;
import com.asadaclan.accounting.entities.Undo;
import com.asadaclan.accounting.enums.LedgerEntryType;

class MessageDelegationServiceTests {

    private TransactionService transactionService;
    private PayoutService payoutService;
    private ExpenseService expenseService;
    private UndoService undoService;
    private HelpService helpService;
    private MessageDelegationService delegationService;

    @BeforeEach
    void setUp() {
        transactionService = mock(TransactionService.class);
        payoutService = mock(PayoutService.class);
        expenseService = mock(ExpenseService.class);
        undoService = mock(UndoService.class);
        helpService = mock(HelpService.class);
        delegationService = new MessageDelegationService(
                transactionService, payoutService, expenseService, undoService, helpService);
    }

    @Test
    void delegatesTransactions() {
        Transaction message = new Transaction(
                new BigDecimal("100.00"), new BigDecimal("20.00"), "Customer", LocalDate.of(2026, 10, 10));
        UUID savedId = UUID.fromString("3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e47");
        when(transactionService.processTransaction(message)).thenReturn(message.withId(savedId));

        String result = delegationService.delegate(message);

        assertThat(result).isEqualTo("transactionId: " + savedId);
        verify(transactionService).processTransaction(message);
    }

    @Test
    void delegatesExpenses() {
        Expense message = new Expense(new BigDecimal("25.00"), "Supplies", LocalDate.of(2026, 10, 10));
        UUID savedId = UUID.fromString("6b2e1d40-0c5a-4f93-a871-9e4c3d2b7a16");
        when(expenseService.processExpense(message)).thenReturn(message.withId(savedId));

        String result = delegationService.delegate(message);

        assertThat(result).isEqualTo("transactionId: " + savedId);
        verify(expenseService).processExpense(message);
    }

    @Test
    void delegatesPayouts() {
        Payout message = new Payout(new BigDecimal("500.00"), LocalDate.of(2026, 10, 10));
        UUID savedId = UUID.fromString("8a9c01f2-19ba-4a53-a166-5485c1b8f31b");
        when(payoutService.processPayout(message)).thenReturn(message.withId(savedId));

        String result = delegationService.delegate(message);

        assertThat(result).isEqualTo("transactionId: " + savedId);
        verify(payoutService).processPayout(message);
    }

    @Test
    void delegatesUndoRequests() {
        UUID entryId = UUID.fromString("3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e47");
        UUID undoId = UUID.fromString("9bb95431-c621-4c4d-a11a-f73510c8d955");
        Undo message = new Undo(LedgerEntryType.EXPENSE, entryId);
        when(undoService.undo(message)).thenReturn(message.withId(undoId));

        String result = delegationService.delegate(message);

        assertThat(result).isEqualTo(
                "Undo(" + undoId + ") Successfully undone expense with ID: " + entryId);
        verify(undoService).undo(message);
    }

    @Test
    void delegatesHelpRequests() {
        Help message = new Help();
        when(helpService.processHelp(message)).thenReturn("command syntax");

        assertThat(delegationService.delegate(message)).isEqualTo("command syntax");
        verify(helpService).processHelp(message);
    }
}
