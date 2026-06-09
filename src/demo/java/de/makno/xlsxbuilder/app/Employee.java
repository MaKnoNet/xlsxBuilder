package de.makno.xlsxbuilder.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Demo data type whose fields cover all column types. {@code checkInSeconds} is the check-in time as
 * seconds since midnight (raw value {@code int}), which is converted into a time of day via a
 * converter. Used both by {@link XlsxBuilderDemo} (in-memory generator) and by {@link DbBenchmark}
 * (read from the H2 DB).
 */
public record Employee(
        long id,
        String name,
        String department,
        int age,
        double rating,
        BigDecimal salary,
        boolean active,
        LocalDate hireDate,
        LocalDateTime lastLogin,
        int checkInSeconds) {}
