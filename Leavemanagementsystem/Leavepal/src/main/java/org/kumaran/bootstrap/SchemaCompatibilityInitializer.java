package org.kumaran.bootstrap;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class SchemaCompatibilityInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    public SchemaCompatibilityInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!usersTableExists()) {
            return;
        }

        jdbcTemplate.execute(
                "alter table users add column if not exists password_reset_requested boolean not null default false");
        jdbcTemplate.execute("alter table users add column if not exists password_reset_requested_at varchar(255)");
        jdbcTemplate.execute(
                "alter table users add column if not exists force_password_change boolean not null default false");
        jdbcTemplate.execute("alter table users add column if not exists temporary_password_issued_at varchar(255)");
    }

    private boolean usersTableExists() {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) {
            return false;
        }

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            return tableExists(metadata, "users")
                    || tableExists(metadata, "USERS")
                    || tableExists(metadata, "Users");
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to inspect database metadata", ex);
        }
    }

    private boolean tableExists(DatabaseMetaData metadata, String tableName) throws SQLException {
        try (ResultSet tables = metadata.getTables(null, null, tableName, new String[] { "TABLE" })) {
            return tables.next();
        }
    }
}
