package com.asadaclan.accounting.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.asadaclan.accounting.config.MySqlTestContainerConfiguration;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MySqlTestContainerConfiguration.class)
class LedgerReportQueryTests {

    private static final String BUSINESS_ID = "+15555550100";
    private static final String OTHER_BUSINESS_ID = "+15555550101";
    private static final LocalDate START_DATE = LocalDate.of(2026, 2, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 2, 28);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PayoutRepository payoutRepository;

    @Test
    void reportQueriesExcludeOnlyMatchingUndosForTheSameBusinessAndEntryType() {
        UUID activeTransactionId = UUID.randomUUID();
        UUID undoneTransactionId = UUID.randomUUID();
        UUID activeExpenseId = UUID.randomUUID();
        UUID undoneExpenseId = UUID.randomUUID();
        UUID activePayoutId = UUID.randomUUID();
        UUID undonePayoutId = UUID.randomUUID();

        insertBusiness(BUSINESS_ID);
        insertBusiness(OTHER_BUSINESS_ID);
        insertTransaction(activeTransactionId, "Active client", LocalDate.of(2026, 2, 2));
        insertTransaction(undoneTransactionId, "Undone client", LocalDate.of(2026, 2, 3));
        insertExpense(activeExpenseId, "Active expense", LocalDate.of(2026, 2, 4));
        insertExpense(undoneExpenseId, "Undone expense", LocalDate.of(2026, 2, 5));
        insertPayout(activePayoutId, LocalDate.of(2026, 2, 6));
        insertPayout(undonePayoutId, LocalDate.of(2026, 2, 7));

        insertUndo(BUSINESS_ID, "TRANSACTION", undoneTransactionId);
        insertUndo(BUSINESS_ID, "EXPENSE", undoneExpenseId);
        insertUndo(BUSINESS_ID, "PAYOUT", undonePayoutId);
        insertUndo(OTHER_BUSINESS_ID, "TRANSACTION", activeTransactionId);
        insertUndo(BUSINESS_ID, "PAYOUT", activeExpenseId);

        assertThat(transactionRepository.findAllNotUndoneByBusinessIdAndDateBetweenOrderByDateAsc(
                BUSINESS_ID, START_DATE, END_DATE))
                .extracting(transaction -> transaction.id())
                .containsExactly(activeTransactionId);
        assertThat(expenseRepository.findAllNotUndoneByBusinessIdAndDateBetweenOrderByDateAsc(
                BUSINESS_ID, START_DATE, END_DATE))
                .extracting(expense -> expense.id())
                .containsExactly(activeExpenseId);
        assertThat(payoutRepository.findAllNotUndoneByBusinessIdAndDateBetweenOrderByDateAsc(
                BUSINESS_ID, START_DATE, END_DATE))
                .extracting(payout -> payout.id())
                .containsExactly(activePayoutId);
    }

    private void insertBusiness(String businessId) {
        jdbcTemplate.update(
                "INSERT INTO businesses (phone_number, name, email) VALUES (?, ?, ?)",
                businessId, "Test business", businessId + "@example.com");
    }

    private void insertTransaction(UUID id, String name, LocalDate date) {
        jdbcTemplate.update("""
                INSERT INTO transactions (id, business_id, price, tip, name, business_date)
                VALUES (?, ?, 100.00, 10.00, ?, ?)
                """, id.toString(), BUSINESS_ID, name, date);
    }

    private void insertExpense(UUID id, String reason, LocalDate date) {
        jdbcTemplate.update("""
                INSERT INTO expenses (id, business_id, cost, reason, business_date)
                VALUES (?, ?, 20.00, ?, ?)
                """, id.toString(), BUSINESS_ID, reason, date);
    }

    private void insertPayout(UUID id, LocalDate date) {
        jdbcTemplate.update("""
                INSERT INTO payouts (id, business_id, amount, business_date)
                VALUES (?, ?, 30.00, ?)
                """, id.toString(), BUSINESS_ID, date);
    }

    private void insertUndo(String businessId, String entryType, UUID entryId) {
        jdbcTemplate.update("""
                INSERT INTO `undos` (business_id, entry_type, entry_id)
                VALUES (?, ?, ?)
                """, businessId, entryType, entryId.toString());
    }
}
