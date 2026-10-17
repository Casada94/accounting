package com.asadaclan.accounting.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.asadaclan.accounting.exceptions.CommandParseException;
import com.asadaclan.accounting.exceptions.ParseErrorCode;
import com.asadaclan.accounting.entities.Report;
import com.asadaclan.accounting.util.DateParser;

class ReportParserTests {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Los_Angeles");

    @Test
    void defaultsFollowTheBusinessDateAcrossMidnightBoundaries() {
        ReportParser beforeYear = parserAt("2026-01-01T07:59:00Z");
        ReportParser atYear = parserAt("2026-01-01T08:00:00Z");
        ReportParser beforeMonth = parserAt("2026-11-01T06:59:00Z");
        ReportParser atMonth = parserAt("2026-11-01T07:00:00Z");

        assertEquals("2025-12", period(beforeYear, "report, month"));
        assertEquals("2025", period(beforeYear, "report, year"));
        assertEquals("2025-03", period(beforeYear, "report, month, 3"));
        assertEquals("2024-12", period(beforeYear, "report, month, , 2024"));

        assertEquals("2026-01", period(atYear, "report, month"));
        assertEquals("2026", period(atYear, "report, year"));
        assertEquals("2026-03", period(atYear, "report, month, march"));
        assertEquals("2024-01", period(atYear, "report, month, , 2024"));

        assertEquals("2026-10", period(beforeMonth, "report, month"));
        assertEquals("2026-11", period(atMonth, "report, month"));
        assertEquals("2026", period(beforeMonth, "report, year"));
        assertEquals("2026", period(atMonth, "report, year"));
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

    private static String period(ReportParser parser, String message) {
        Report command = parser.parse(fields(message));
        return command.reportPeriod().orElseThrow();
    }

    private static ReportParser parserAt(String instant) {
        return new ReportParser(new DateParser(Clock.fixed(Instant.parse(instant), BUSINESS_ZONE)));
    }

    private static List<String> fields(String message) {
        return Arrays.asList(message.split(",", -1));
    }
}
