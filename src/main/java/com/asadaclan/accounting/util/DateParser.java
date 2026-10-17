package com.asadaclan.accounting.util;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Parses business-local dates, transaction dates, months, and years.
 * Callers translate failures into command field errors.
 */
@Component
public final class DateParser {
    private final Clock clock;

    @Autowired
    public DateParser(@Value("${accounting.time-zone}") String businessZone) {
        this(Clock.system(ZoneId.of(businessZone)));
    }

    public DateParser(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public LocalDate businessDate() {
        return LocalDate.now(clock);
    }

    public LocalDate parseTransactionDate(String raw) {
        LocalDate businessDate = businessDate();
        if (raw == null) {
            throw new DateTimeParseException("Invalid date", "", 0);
        }
        String keyword = raw.toLowerCase(Locale.ROOT);
        if (keyword.equals("today")) {
            return businessDate;
        }
        if (keyword.equals("yesterday")) {
            return businessDate.minusDays(1);
        }
        try {
            if (raw.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
                return LocalDate.parse(raw);
            }
            if (raw.matches("[0-9]{1,2}/[0-9]{1,2}")) {
                String[] dateParts = raw.split("/");
                return LocalDate.of(businessDate.getYear(), Integer.parseInt(dateParts[0]),
                        Integer.parseInt(dateParts[1]));
            }
            if (raw.matches("[0-9]{1,2}/[0-9]{1,2}/[0-9]{4}")) {
                String[] dateParts = raw.split("/");
                return LocalDate.of(Integer.parseInt(dateParts[2]), Integer.parseInt(dateParts[0]),
                        Integer.parseInt(dateParts[1]));
            }
        } catch (DateTimeParseException exception) {
            throw exception;
        } catch (DateTimeException exception) {
            throw new DateTimeParseException("Invalid date", raw, 0, exception);
        }
        throw new DateTimeParseException("Invalid date", raw, 0);
    }

    public int parseMonth(String raw) {
        if (raw == null) {
            throw new DateTimeParseException("Invalid month", "", 0);
        }
        String monthText = raw.toLowerCase(Locale.ENGLISH);
        if (monthText.matches("[0-9]{1,2}")) {
            int month = Integer.parseInt(monthText);
            if (month >= 1 && month <= 12) {
                return month;
            }
            throw new DateTimeParseException("Invalid month", raw, 0);
        }
        return switch (monthText) {
            case "jan", "january" -> 1;
            case "feb", "february" -> 2;
            case "mar", "march" -> 3;
            case "apr", "april" -> 4;
            case "may" -> 5;
            case "jun", "june" -> 6;
            case "jul", "july" -> 7;
            case "aug", "august" -> 8;
            case "sep", "september" -> 9;
            case "oct", "october" -> 10;
            case "nov", "november" -> 11;
            case "dec", "december" -> 12;
            default -> throw new DateTimeParseException("Invalid month", raw, 0);
        };
    }

    public Year parseYear(String raw) {
        if (raw == null || !raw.matches("[0-9]{4}")) {
            throw new DateTimeParseException("Invalid year", raw == null ? "" : raw, 0);
        }
        try {
            return Year.parse(raw);
        } catch (DateTimeParseException exception) {
            throw exception;
        } catch (DateTimeException exception) {
            throw new DateTimeParseException("Invalid year", raw, 0, exception);
        }
    }
}
