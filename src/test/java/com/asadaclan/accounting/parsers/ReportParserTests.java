package com.asadaclan.accounting.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.asadaclan.accounting.exceptions.CommandParseException;
import com.asadaclan.accounting.exceptions.ParseErrorCode;
import com.asadaclan.accounting.entities.ReportRequest;
import com.asadaclan.accounting.util.DateParser;

class ReportParserTests {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Los_Angeles");

    @Test
    void defaultsFollowTheBusinessDateAcrossMidnightBoundaries() {
        ReportParser beforeYear = parserAt("2026-01-01T07:59:00Z");
        ReportParser atYear = parserAt("2026-01-01T08:00:00Z");
        ReportParser beforeMonth = parserAt("2026-11-01T06:59:00Z");
        ReportParser atMonth = parserAt("2026-11-01T07:00:00Z");

        assertEquals(LocalDate.of(2025, 12, 1), startDate(beforeYear, "report, month"));
        assertEquals(LocalDate.of(2025, 1, 1), startDate(beforeYear, "report, year"));
        assertEquals(LocalDate.of(2025, 3, 1), startDate(beforeYear, "report, month, 3"));
        assertEquals(LocalDate.of(2024, 12, 1), startDate(beforeYear, "report, month, , 2024"));

        assertEquals(LocalDate.of(2026, 1, 1), startDate(atYear, "report, month"));
        assertEquals(LocalDate.of(2026, 1, 1), startDate(atYear, "report, year"));
        assertEquals(LocalDate.of(2026, 3, 1), startDate(atYear, "report, month, march"));
        assertEquals(LocalDate.of(2024, 1, 1), startDate(atYear, "report, month, , 2024"));

        assertEquals(LocalDate.of(2026, 10, 1), startDate(beforeMonth, "report, month"));
        assertEquals(LocalDate.of(2026, 11, 1), startDate(atMonth, "report, month"));
        assertEquals(LocalDate.of(2026, 1, 1), startDate(beforeMonth, "report, year"));
        assertEquals(LocalDate.of(2026, 1, 1), startDate(atMonth, "report, year"));
    }

    @Test
    void translatesMonthAndYearFailuresToTheFieldThatFailed() {
        ReportParser parser = parserAt("2026-10-09T19:00:00Z");

        CommandParseException invalidMonth = assertThrows(CommandParseException.class,
                () -> parser.parse(fields("report, month, 13")));
        assertEquals(ParseErrorCode.INVALID_MONTH, invalidMonth.code());
        assertEquals(2, invalidMonth.fieldIndex());
        assertEquals("month", invalidMonth.fieldName());

        CommandParseException invalidMonthYear = assertThrows(CommandParseException.class,
                () -> parser.parse(fields("report, month, march, 25")));
        assertEquals(ParseErrorCode.INVALID_YEAR, invalidMonthYear.code());
        assertEquals(3, invalidMonthYear.fieldIndex());
        assertEquals("year", invalidMonthYear.fieldName());

        CommandParseException invalidYear = assertThrows(CommandParseException.class,
                () -> parser.parse(fields("report, year, march")));
        assertEquals(ParseErrorCode.INVALID_YEAR, invalidYear.code());
        assertEquals(2, invalidYear.fieldIndex());
        assertEquals("year", invalidYear.fieldName());
    }

    @Test
    void setsInclusiveStartAndEndDatesForMonthlyReports() {
        ReportRequest report = parserAt("2026-10-09T19:00:00Z")
                .parse(fields("report, month, february, 2024"));

        assertEquals(LocalDate.of(2024, 2, 1), report.startDate());
        assertEquals(LocalDate.of(2024, 2, 29), report.endDate());
    }

    @Test
    void setsInclusiveStartAndEndDatesForYearlyReports() {
        ReportRequest report = parserAt("2026-10-09T19:00:00Z")
                .parse(fields("report, year, 2024"));

        assertEquals(LocalDate.of(2024, 1, 1), report.startDate());
        assertEquals(LocalDate.of(2024, 12, 31), report.endDate());
    }

    private static LocalDate startDate(ReportParser parser, String message) {
        ReportRequest command = parser.parse(fields(message));
        return command.startDate();
    }

    private static ReportParser parserAt(String instant) {
        return new ReportParser(new DateParser(Clock.fixed(Instant.parse(instant), BUSINESS_ZONE)));
    }

    private static List<String> fields(String message) {
        return Arrays.asList(message.split(",", -1));
    }
}
