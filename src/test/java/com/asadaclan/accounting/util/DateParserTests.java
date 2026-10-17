package com.asadaclan.accounting.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DateParserTests {
    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 10, 9);
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Los_Angeles");
    private final DateParser parser = parserAt("2026-10-09T19:00:00Z", BUSINESS_ZONE);

    @Test
    void parsesTodayAndYesterdayFromTheSuppliedBusinessDate() {
        assertEquals(BUSINESS_DATE, parser.parseTransactionDate("today"));
        assertEquals(BUSINESS_DATE, parser.parseTransactionDate("TODAY"));
        assertEquals(LocalDate.of(2026, 10, 8),
                parser.parseTransactionDate("yesterday"));
        assertEquals(LocalDate.of(2026, 10, 8),
                parser.parseTransactionDate("Yesterday"));

        assertEquals(LocalDate.of(2025, 12, 31),
                parserAt("2026-01-01T12:00:00Z", ZoneId.of("UTC")).parseTransactionDate("yesterday"));
    }

    @Test
    void parsesIsoMonthDayAndFullNumericDates() {
        assertEquals(LocalDate.of(2026, 10, 5),
                parser.parseTransactionDate("2026-10-05"));
        assertEquals(LocalDate.of(2026, 10, 5),
                parser.parseTransactionDate("10/5"));
        assertEquals(LocalDate.of(2026, 10, 5),
                parser.parseTransactionDate("10/05"));
        assertEquals(LocalDate.of(2025, 10, 5),
                parser.parseTransactionDate("10/5/2025"));
        assertEquals(LocalDate.of(2025, 10, 5),
                parser.parseTransactionDate("10/05/2025"));
        assertEquals(LocalDate.of(2026, 12, 31),
                parserAt("2026-01-01T12:00:00Z", ZoneId.of("UTC")).parseTransactionDate("12/31"));
    }

    @Test
    void acceptsLeapDaysAndRejectsImpossibleDates() {
        DateParser leapYearParser = parserAt("2024-03-01T12:00:00Z", ZoneId.of("UTC"));
        assertEquals(LocalDate.of(2024, 2, 29),
                leapYearParser.parseTransactionDate("2024-02-29"));
        assertEquals(LocalDate.of(2024, 2, 29),
                leapYearParser.parseTransactionDate("2/29"));
        assertEquals(LocalDate.of(2024, 2, 29),
                parser.parseTransactionDate("2/29/2024"));

        assertInvalidDate("2026-02-29");
        assertInvalidDate("2/29");
        assertInvalidDate("2/29/2026");
        assertInvalidDate("2/30");
        assertInvalidDate("4/31");
        assertInvalidDate("13/1");
        assertInvalidDate("2026-04-31");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2026-10-5",
            "2026-1-01",
            "20261005",
            "2026/10/05",
            "10-05-2026",
            "2026-13-01",
            "tomorrow",
            ""
    })
    void rejectsMalformedDates(String raw) {
        assertInvalidDate(raw);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1",
            "01, 1",
            "12, 12",
            "jan, 1",
            "January, 1",
            "FEB, 2",
            "february, 2",
            "mar, 3",
            "MARCH, 3",
            "apr, 4",
            "April, 4",
            "may, 5",
            "jun, 6",
            "June, 6",
            "jul, 7",
            "July, 7",
            "aug, 8",
            "August, 8",
            "sep, 9",
            "September, 9",
            "oct, 10",
            "oCtObEr, 10",
            "nov, 11",
            "November, 11",
            "dec, 12",
            "December, 12"
    })
    void parsesNumericAbbreviatedAndFullMonthNames(String raw, int month) {
        assertEquals(month, parser.parseMonth(raw));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "00", "13", "sept", "marchh", "", "3rd"})
    void rejectsInvalidMonths(String raw) {
        assertThrows(DateTimeParseException.class, () -> parser.parseMonth(raw));
    }

    @Test
    void acceptsExactlyFourDigitYearsAndRejectsOtherYearText() {
        assertEquals(Year.of(2026), parser.parseYear("2026"));
        assertEquals(Year.of(1), parser.parseYear("0001"));
        assertEquals(Year.of(9999), parser.parseYear("9999"));

        assertThrows(DateTimeParseException.class, () -> parser.parseYear("25"));
        assertThrows(DateTimeParseException.class, () -> parser.parseYear("20261"));
        assertThrows(DateTimeParseException.class, () -> parser.parseYear("march"));
        assertThrows(DateTimeParseException.class, () -> parser.parseYear("2026a"));
        assertThrows(DateTimeParseException.class, () -> parser.parseYear(""));
        assertThrows(DateTimeParseException.class, () -> parser.parseYear(null));
    }

    @Test
    void resolvesTheBusinessDateAcrossZoneMidnightBoundaries() {
        assertEquals(LocalDate.of(2026, 10, 9),
                parserAt("2026-10-10T06:59:00Z", BUSINESS_ZONE).businessDate());
        assertEquals(LocalDate.of(2026, 10, 10),
                parserAt("2026-10-10T07:00:00Z", BUSINESS_ZONE).businessDate());
        assertEquals(LocalDate.of(2025, 12, 31),
                parserAt("2026-01-01T07:59:00Z", BUSINESS_ZONE).businessDate());
        assertEquals(LocalDate.of(2026, 1, 1),
                parserAt("2026-01-01T08:00:00Z", BUSINESS_ZONE).businessDate());
        assertEquals(LocalDate.of(2026, 10, 31),
                parserAt("2026-11-01T06:59:00Z", BUSINESS_ZONE).businessDate());
        assertEquals(LocalDate.of(2026, 11, 1),
                parserAt("2026-11-01T07:00:00Z", BUSINESS_ZONE).businessDate());

        Instant sharedInstant = Instant.parse("2026-10-10T01:00:00Z");
        assertEquals(LocalDate.of(2026, 10, 9),
                new DateParser(Clock.fixed(sharedInstant, BUSINESS_ZONE)).businessDate());
        assertEquals(LocalDate.of(2026, 10, 10),
                new DateParser(Clock.fixed(sharedInstant, ZoneId.of("Asia/Tokyo"))).businessDate());
    }

    private void assertInvalidDate(String raw) {
        assertThrows(DateTimeParseException.class,
                () -> parser.parseTransactionDate(raw));
    }

    private static DateParser parserAt(String instant, ZoneId zoneId) {
        return new DateParser(Clock.fixed(Instant.parse(instant), zoneId));
    }
}
