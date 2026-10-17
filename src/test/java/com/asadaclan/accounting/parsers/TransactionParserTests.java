package com.asadaclan.accounting.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import com.asadaclan.accounting.entities.Transaction;
import com.asadaclan.accounting.exceptions.CommandParseException;
import com.asadaclan.accounting.exceptions.ParseErrorCode;
import com.asadaclan.accounting.util.DateParser;

class TransactionParserTests {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Los_Angeles");

    private TransactionParser parser;

    @BeforeEach
    void setUp() {
        parser = new TransactionParser(dateParserAt("2026-10-09T19:00:00Z", BUSINESS_ZONE));
    }

    @Test
    void parsesTattooWithDefaults() {
        Transaction command = parse("tattoo, 250, 50");

        assertEquals(new BigDecimal("250.00"), command.price());
        assertEquals(new BigDecimal("50.00"), command.tip());
        assertNull(command.name());
        assertEquals(LocalDate.of(2026, 10, 9), command.date());
    }

    @Test
    void parsesCurrencyNameAndRelativeDate() {
        Transaction command = parse("TATTOO, $250.5, $0, Sarah, yesterday");

        assertEquals(new BigDecimal("250.50"), command.price());
        assertEquals(new BigDecimal("0.00"), command.tip());
        assertEquals("Sarah", command.name());
        assertEquals(LocalDate.of(2026, 10, 8), command.date());
    }

    @Test
    void keepsOptionalFieldsPositional() {
        Transaction namedYesterday = parse("tattoo, 250, 50, yesterday");
        Transaction omittedName = parse("tattoo, 250, 50, , 10/5");

        assertEquals("yesterday", namedYesterday.name());
        assertEquals(LocalDate.of(2026, 10, 9), namedYesterday.date());
        assertNull(omittedName.name());
        assertEquals(LocalDate.of(2026, 10, 5), omittedName.date());
    }

    @ParameterizedTest
    @CsvSource(nullValues = "NULL", value = {
            "'tattoo, 250', MISSING_REQUIRED_FIELD, 2, tip",
            "'tattoo, 250, -5', INVALID_AMOUNT, 2, tip",
            "'tattoo, 0, 5', INVALID_AMOUNT, 1, price",
            "'tattoo, 250, 50, Sarah, 2/30', INVALID_DATE, 4, date",
            "'tattoo, 250, 50, Sarah, today, extra', WRONG_ARGUMENT_COUNT, -1, NULL"
    })
    void returnsStructuredErrors(
            String input, ParseErrorCode expectedCode, int expectedFieldIndex, String expectedFieldName) {
        CommandParseException exception = assertThrows(
                CommandParseException.class, () -> parser.parse(fields(input)));

        assertEquals(expectedCode, exception.code());
        assertEquals(expectedFieldIndex, exception.fieldIndex());
        assertEquals(expectedFieldName, exception.fieldName());
    }

    @Test
    void rejectsMissingAndMultilineMessages() {
        assertEquals(ParseErrorCode.EMPTY_MESSAGE,
                assertThrows(CommandParseException.class, () -> parser.parse(null)).code());
        assertEquals(ParseErrorCode.EMPTY_MESSAGE,
                assertThrows(CommandParseException.class, () -> parser.parse(List.of())).code());
        assertEquals(ParseErrorCode.MISSING_REQUIRED_FIELD,
                assertThrows(CommandParseException.class, () -> parser.parse(fields("   "))).code());
        assertEquals(ParseErrorCode.WRONG_ARGUMENT_COUNT,
                assertThrows(CommandParseException.class,
                        () -> parser.parse(fields("tattoo, 250, 50\ntattoo, 100, 10"))).code());
    }

    @Test
    void usesBusinessZoneAtMidnightBoundary() {
        Instant instant = Instant.parse("2026-10-10T01:00:00Z");
        TransactionParser losAngelesParser = new TransactionParser(
                new DateParser(Clock.fixed(instant, ZoneId.of("America/Los_Angeles"))));
        TransactionParser tokyoParser = new TransactionParser(
                new DateParser(Clock.fixed(instant, ZoneId.of("Asia/Tokyo"))));

        assertEquals(LocalDate.of(2026, 10, 9),
                losAngelesParser.parse(fields("tattoo, 250, 50")).date());
        assertEquals(LocalDate.of(2026, 10, 10),
                tokyoParser.parse(fields("tattoo, 250, 50")).date());
    }

    private Transaction parse(String message) {
        return parser.parse(fields(message));
    }

    private static List<String> fields(String message) {
        return Arrays.asList(message.split(",", -1));
    }

    private static DateParser dateParserAt(String instant, ZoneId zoneId) {
        return new DateParser(Clock.fixed(Instant.parse(instant), zoneId));
    }
}
