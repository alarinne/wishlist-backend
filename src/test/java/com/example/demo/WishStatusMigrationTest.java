package com.example.demo;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WishStatusMigrationTest {

    @Test
    void migrateFromV1_preservesExistingWishAndAllowsOnlySupportedStatuses() throws Exception {
        String databaseUrl = "jdbc:h2:mem:wish-status-" + UUID.randomUUID();
        HikariConfig configuration = new HikariConfig();
        configuration.setJdbcUrl(databaseUrl);
        configuration.setUsername("sa");
        configuration.setPassword("");
        configuration.setMaximumPoolSize(4);
        configuration.setConnectionTimeout(5000);

        // Use the same pooled connection lifecycle as the application during Flyway upgrades.
        try (var dataSource = new HikariDataSource(configuration);
             var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .target("1")
                    .load()
                    .migrate();

            statement.executeUpdate("""
                    INSERT INTO category (id, name, code, label)
                    VALUES (1, 'Books', 'books-migration', 'Books')
                    """);
            statement.executeUpdate("""
                    INSERT INTO wish (wish_id, wish_name, wish_price, url, status, category_id, priority)
                    VALUES (1, 'Kindle', 120.0, 'https://example.com/kindle', 'ACTIVE', 1, 'HIGH')
                    """);
            assertThrows(SQLException.class, () ->
                    statement.executeUpdate("UPDATE wish SET status = 'PURCHASED' WHERE wish_id = 1"));

            var result = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .load()
                    .migrate();

            assertThat(result.migrationsExecuted).isEqualTo(1);
            try (var rows = statement.executeQuery("SELECT * FROM wish WHERE wish_id = 1")) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getString("status")).isEqualTo("ACTIVE");
                assertThat(rows.getString("wish_name")).isEqualTo("Kindle");
                assertThat(rows.getDouble("wish_price")).isEqualTo(120.0);
                assertThat(rows.getString("url")).isEqualTo("https://example.com/kindle");
                assertThat(rows.getLong("category_id")).isEqualTo(1);
                assertThat(rows.getString("priority")).isEqualTo("HIGH");
                assertThat(rows.next()).isFalse();
            }

            assertThat(statement.executeUpdate("UPDATE wish SET status = 'PURCHASED' WHERE wish_id = 1"))
                    .isEqualTo(1);
            try (var rows = statement.executeQuery("SELECT status FROM wish WHERE wish_id = 1")) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getString("status")).isEqualTo("PURCHASED");
            }

            assertThat(statement.executeUpdate("UPDATE wish SET status = 'ACTIVE' WHERE wish_id = 1"))
                    .isEqualTo(1);
            SQLException invalidStatus = assertThrows(SQLException.class, () ->
                    statement.executeUpdate("UPDATE wish SET status = 'UNKNOWN' WHERE wish_id = 1"));
            assertThat(invalidStatus.getSQLState()).isEqualTo("23513");

            try (var rows = statement.executeQuery("SELECT status FROM wish WHERE wish_id = 1")) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getString("status")).isEqualTo("ACTIVE");
            }
        }
    }
}
