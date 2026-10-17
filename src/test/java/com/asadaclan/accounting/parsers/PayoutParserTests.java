package com.asadaclan.accounting.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.asadaclan.accounting.exceptions.CommandParseException;
import com.asadaclan.accounting.exceptions.ParseErrorCode;
import com.asadaclan.accounting.entities.Payout;
import com.asadaclan.accounting.util.DateParser;

class PayoutParserTests {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Los_Angeles");

    private PayoutParser parser;

    @BeforeEach
    void setUp() {
        parser = new PayoutParser(dateParserAt("2026-10-09T19:00:00Z", BUSINESS_ZONE));
    }

    @ParameterizedTest
    @CsvSource({
            "'payout, 500', 500.00",
            "'payout, $125.50', 125.50",
            "'PAYOUT, $125.5', 125.50",
            "'  payout  ,  1  ', 1.00"
    })
    void parsesPositiveAmounts(String input, String expectedAmount) {
        Payout command = parser.parse(fields(input));

        assertEquals(new BigDecimal(expectedAmount), command.amount());
        assertEquals(LocalDate.of(2026, 10, 9), command.date());
    }

    @ParameterizedTest
    @CsvSource({
            "'payout, 500, today', 2026-10-09",
            "'payout, 500, yesterday', 2026-10-08",
            "'payout, 500, 10/5', 2026-10-05",
            "'payout, 500, 10/5/2025', 2025-10-05",
            "'payout, 500, 2024-02-29', 2024-02-29"
    })
    void parsesOptionalDate(String input, String expectedDate) {
        Payout command = parser.parse(fields(input));

        assertEquals(LocalDate.parse(expectedDate), command.date());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "payout, 0",
            "payout, $0.00",
            "payout, -10",
            "payout, +10",
            "payout, 12.345",
            "payout, .50",
            "payout, 1e3",
            "payout, NaN"
    })
    void rejectsInvalidAmounts(String input) {
        CommandParseException exception = assertThrows(CommandParseException.class,
                () -> parser.parse(fields(input)));

        assertEquals(ParseErrorCode.INVALID_AMOUNT, exception.code());
        assertEquals(1, exception.fieldIndex());
        assertEquals("amount", exception.fieldName());
    }

    @Test
    void rejectsMissingAmount() {
        assertError("payout", ParseErrorCode.MISSING_REQUIRED_FIELD, 1, "amount");
        assertError("payout,", ParseErrorCode.MISSING_REQUIRED_FIELD, 1, "amount");
    }

    @Test
    void allowsAnEmptyOptionalDateAndRejectsExtraFields() {
        Payout command = parser.parse(fields("payout, 500,"));

        assertEquals(LocalDate.of(2026, 10, 9), command.date());
        assertError("payout, 500, extra", ParseErrorCode.INVALID_DATE, 2, "date");
        assertError("payout, 500, today, extra", ParseErrorCode.WRONG_ARGUMENT_COUNT, -1, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "payout, 500, 2/30",
            "payout, 500, 2026-02-29",
            "payout, 500, 2026-10-5",
            "payout, 500, 10/5/26"
    })
    void rejectsInvalidDates(String input) {
        assertError(input, ParseErrorCode.INVALID_DATE, 2, "date");
    }

    @Test
    void usesTheConfiguredBusinessZoneForTheDefaultDate() {
        Instant instant = Instant.parse("2026-10-10T01:00:00Z");
        PayoutParser losAngelesParser = new PayoutParser(
                new DateParser(Clock.fixed(instant, ZoneId.of("America/Los_Angeles"))));
        PayoutParser tokyoParser = new PayoutParser(
                new DateParser(Clock.fixed(instant, ZoneId.of("Asia/Tokyo"))));

        assertEquals(LocalDate.of(2026, 10, 9), losAngelesParser.parse(fields("payout, 500")).date());
        assertEquals(LocalDate.of(2026, 10, 10), tokyoParser.parse(fields("payout, 500")).date());
    }

    @Test
    void rejectsMissingFieldsAndMultilineCommands() {
        assertError((List<String>) null, ParseErrorCode.EMPTY_MESSAGE, 0, "action");
        assertError(List.of(), ParseErrorCode.EMPTY_MESSAGE, 0, "action");
        assertError("   ", ParseErrorCode.MISSING_REQUIRED_FIELD, 1, "amount");
        assertError("payout, 500\npayout, 100", ParseErrorCode.WRONG_ARGUMENT_COUNT, -1, null);
    }

    private void assertError(String input, ParseErrorCode code, int fieldIndex, String fieldName) {
        assertError(input == null ? null : fields(input), code, fieldIndex, fieldName);
    }

    private void assertError(List<String> input, ParseErrorCode code, int fieldIndex, String fieldName) {
        CommandParseException exception = assertThrows(CommandParseException.class, () -> parser.parse(input));
        assertEquals(code, exception.code());
        assertEquals(fieldIndex, exception.fieldIndex());
        assertEquals(fieldName, exception.fieldName());
    }

    private static List<String> fields(String message) {
        return Arrays.asList(message.split(",", -1));
    }

    private static DateParser dateParserAt(String instant, ZoneId zoneId) {
        return new DateParser(Clock.fixed(Instant.parse(instant), zoneId));
    }
}
