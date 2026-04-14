package org.kumaran.bootstrap;

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
        Boolean usersTableExists = jdbcTemplate.queryForObject(
                "select to_regclass('public.users') is not null",
                Boolean.class
        );

        if (!Boolean.TRUE.equals(usersTableExists)) {
            return;
        }

        jdbcTemplate.execute("alter table users add column if not exists password_reset_requested boolean not null default false");
        jdbcTemplate.execute("alter table users add column if not exists password_reset_requested_at varchar(255)");
        jdbcTemplate.execute("alter table users add column if not exists force_password_change boolean not null default false");
        jdbcTemplate.execute("alter table users add column if not exists temporary_password_issued_at varchar(255)");

        Boolean leaveApplicationTableExists = jdbcTemplate.queryForObject(
                "select to_regclass('public.leave_application') is not null",
                Boolean.class
        );
        if (Boolean.TRUE.equals(leaveApplicationTableExists)) {
            jdbcTemplate.execute("alter table leave_application alter column duration type double precision using duration::double precision");
        }

        Boolean leaveTrackerTableExists = jdbcTemplate.queryForObject(
                "select to_regclass('public.leave_tracker') is not null",
                Boolean.class
        );
        if (Boolean.TRUE.equals(leaveTrackerTableExists)) {
            jdbcTemplate.execute("alter table leave_tracker alter column sick_leave_available type double precision using sick_leave_available::double precision");
            jdbcTemplate.execute("alter table leave_tracker alter column casual_leave_available type double precision using casual_leave_available::double precision");
            jdbcTemplate.execute("alter table leave_tracker alter column loss_of_pay_available type double precision using loss_of_pay_available::double precision");
            jdbcTemplate.execute("alter table leave_tracker alter column sick_leave_booked type double precision using sick_leave_booked::double precision");
            jdbcTemplate.execute("alter table leave_tracker alter column casual_leave_booked type double precision using casual_leave_booked::double precision");
            jdbcTemplate.execute("alter table leave_tracker alter column loss_of_pay_booked type double precision using loss_of_pay_booked::double precision");
        }
    }
}

