package com.asadaclan.accounting.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MySqlTestContainerConfiguration.class)
class MySqlTestContainerTests {

    private static final Map<String, List<String>> ENTITY_COLUMNS = Map.of(
            "businesses", List.of("phone_number", "name", "email", "created_at"),
            "transactions",
            List.of("id", "business_id", "price", "tip", "name", "business_date", "created_at"),
            "expenses", List.of("id", "business_id", "cost", "reason", "business_date", "created_at"),
            "payouts", List.of("id", "business_id", "amount", "business_date", "created_at"),
            "reports", List.of("id", "business_id", "start_date", "end_date", "created_at"),
            "undos", List.of("id", "business_id", "entry_type", "entry_id", "created_at"));

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void connectsToMySql() throws SQLException {
        assertThat(jdbcTemplate.getDataSource()).isNotNull();

        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("MySQL");
        }
    }

    @Test
    void flywayCreatesTablesMatchingTheJdbcEntities() {
        List<String> tables = jdbcTemplate.queryForList("""
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name <> 'flyway_schema_history'
                """, String.class);

        assertThat(tables).containsExactlyInAnyOrderElementsOf(ENTITY_COLUMNS.keySet());

        ENTITY_COLUMNS.forEach((table, expectedColumns) -> {
            List<String> columns = jdbcTemplate.queryForList("""
                    SELECT column_name
                    FROM information_schema.columns
                    WHERE table_schema = DATABASE()
                      AND table_name = ?
                    ORDER BY ordinal_position
                    """, String.class, table);

            assertThat(columns).containsExactlyElementsOf(expectedColumns);
        });

        Integer generatedCreatedTimestamps = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND column_name = 'created_at'
                  AND data_type = 'timestamp'
                  AND is_nullable = 'NO'
                  AND UPPER(column_default) LIKE 'CURRENT_TIMESTAMP%'
                """, Integer.class);

        assertThat(generatedCreatedTimestamps).isEqualTo(ENTITY_COLUMNS.size());
    }
}
