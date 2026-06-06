package de.makno.xlsbuilder.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Demo-Datentyp, dessen Felder alle Spaltentypen abdecken. {@code checkInSeconds} ist die Kommt-Zeit
 * als Sekunden seit Mitternacht (Rohwert {@code int}), der per Konverter in eine Uhrzeit umgewandelt
 * wird. Wird sowohl vom {@link ExcelBuilderDemo} (In-Memory-Generator) als auch vom
 * {@link DbBenchmark} (aus der H2-DB gelesen) verwendet.
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
