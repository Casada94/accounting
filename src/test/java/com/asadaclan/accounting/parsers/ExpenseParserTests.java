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

import com.asadaclan.accounting.exceptions.CommandParseException;
import com.asadaclan.accounting.exceptions.ParseErrorCode;
import com.asadaclan.accounting.util.DateParser;

class ExpenseParserTests {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Los_Angeles");

    private ExpenseParser parser;

    @BeforeEach
    void setUp() {
        parser = new ExpenseParser(dateParserAt("2026-10-09T19:00:00Z", BUSINESS_ZONE));
    }

    @Test
    void parsesCostWithDefaultReasonAndBusinessDate() {
        ExpenseCommandAssert parsed = parse("expense, 45");

        parsed.hasCost("45.00").hasNoReason().hasDate(LocalDate.of(2026, 10, 9));
    }

    @Test
    void parsesCurrencyReasonAndExplicitDate() {
        ExpenseCommandAssert omittedDate = parse("expense, $45.25, tattoo ink");
        ExpenseCommandAssert explicitDate = parse("expense, 45, ink, 2026-10-05");

        omittedDate.hasCost("45.25").hasReason("tattoo ink").hasDate(LocalDate.of(2026, 10, 9));
        explicitDate.hasCost("45.00").hasReason("ink").hasDate(LocalDate.of(2026, 10, 5));
    }

    @Test
    void treatsBlankReasonAsAbsentAndKeepsDatePositional() {
        ExpenseCommandAssert blankReason = parse("expense, 45, , yesterday");
        ExpenseCommandAssert namedYesterday = parse("expense, 125, needles, yesterday");
        ExpenseCommandAssert reasonIsNotDate = parse("expense, 45, yesterday");

        blankReason.hasCost("45.00").hasNoReason().hasDate(LocalDate.of(2026, 10, 8));
        namedYesterday.hasCost("125.00").hasReason("needles").hasDate(LocalDate.of(2026, 10, 8));
        reasonIsNotDate.hasReason("yesterday").hasDate(LocalDate.of(2026, 10, 9));
    }

    @Test
    void acceptsNumericDatesWhitespaceAndCase() {
        ExpenseCommandAssert monthDay = parse("expense, 45, , 10/5");
        ExpenseCommandAssert fullNumeric = parse("  EXPENSE , $45.5 , tattoo  ink , 10/05/2025 ");
        ExpenseCommandAssert packed = parse("expense,45,ink,today");

        monthDay.hasNoReason().hasDate(LocalDate.of(2026, 10, 5));
        fullNumeric.hasCost("45.50").hasReason("tattoo  ink").hasDate(LocalDate.of(2025, 10, 5));
        packed.hasReason("ink").hasDate(LocalDate.of(2026, 10, 9));
    }

    @Test
    void acceptsLeapDayAndAllowsFutureDates() {
        parse("expense, 45, ink, 2024-02-29").hasDate(LocalDate.of(2024, 2, 29));
        parse("expense, 45, ink, 2/29/2024").hasDate(LocalDate.of(2024, 2, 29));
        parse("expense, 45, booth, 2027-01-01").hasDate(LocalDate.of(2027, 1, 1));
    }

    @Test
    void allowsBlankOptionalTrailingFields() {
        parse("expense, 45,").hasCost("45.00").hasNoReason().hasDate(LocalDate.of(2026, 10, 9));
        parse("expense, 45, ,").hasNoReason().hasDate(LocalDate.of(2026, 10, 9));
    }

    @ParameterizedTest
    @CsvSource({
            "'expense', MISSING_REQUIRED_FIELD, 1, cost",
            "'expense,', MISSING_REQUIRED_FIELD, 1, cost",
            "'expense, 12.345', INVALID_AMOUNT, 1, cost",
            "'expense, -5', INVALID_AMOUNT, 1, cost",
            "'expense, 0', INVALID_AMOUNT, 1, cost",
            "'expense, $0.00', INVALID_AMOUNT, 1, cost",
            "'expense, .50', INVALID_AMOUNT, 1, cost",
            "'expense, 1e3', INVALID_AMOUNT, 1, cost",
            "'expense, 45, ink, 2/30', INVALID_DATE, 3, date",
            "'expense, 45, ink, 2026-02-29', INVALID_DATE, 3, date",
            "'expense, 45, ink, 2026-10-5', INVALID_DATE, 3, date",
            "'expense, 45, ink, 10/5/26', INVALID_DATE, 3, date",
            "'expense, 45, ink, 2026-10-05, extra', WRONG_ARGUMENT_COUNT, -1,"
    })
    void returnsStructuredErrors(String input, ParseErrorCode expectedCode, int fieldIndex, String fieldName) {
        CommandParseException exception = assertThrows(CommandParseException.class,
                () -> parser.parse(fields(input)));

        assertEquals(expectedCode, exception.code());
        assertEquals(fieldIndex, exception.fieldIndex());
        assertEquals(fieldName, exception.fieldName());
    }

    @Test
    void rejectsMissingFieldsAndASecondCommand() {
        assertEquals(ParseErrorCode.EMPTY_MESSAGE,
                assertThrows(CommandParseException.class, () -> parser.parse(null)).code());
        assertEquals(ParseErrorCode.EMPTY_MESSAGE,
                assertThrows(CommandParseException.class, () -> parser.parse(List.of())).code());
        assertEquals(ParseErrorCode.MISSING_REQUIRED_FIELD,
                assertThrows(CommandParseException.class, () -> parser.parse(fields("   "))).code());
        assertEquals(ParseErrorCode.WRONG_ARGUMENT_COUNT,
                assertThrows(CommandParseException.class, () -> parser.parse(fields("expense, 45\nexpense, 10")))
                        .code());
    }

    @Test
    void usesBusinessZoneWhenTheUtcDateDiffers() {
        Instant instant = Instant.parse("2026-10-10T01:00:00Z");
        ExpenseParser losAngelesParser = new ExpenseParser(
                new DateParser(Clock.fixed(instant, ZoneId.of("America/Los_Angeles"))));
        ExpenseParser tokyoParser = new ExpenseParser(
                new DateParser(Clock.fixed(instant, ZoneId.of("Asia/Tokyo"))));

        assertEquals(LocalDate.of(2026, 10, 9), losAngelesParser.parse(fields("expense, 1")).date());
        assertEquals(LocalDate.of(2026, 10, 8),
                losAngelesParser.parse(fields("expense, 1, , yesterday")).date());
        assertEquals(LocalDate.of(2026, 10, 10), tokyoParser.parse(fields("expense, 1")).date());
    }

    @Test
    void resolvesDefaultsFromTheBusinessDateCapturedAtTheLocalMidnightBoundary() {
        ExpenseParser beforeMidnight = parserAt("2026-10-10T06:59:00Z");
        ExpenseParser atMidnight = parserAt("2026-10-10T07:00:00Z");

        assertEquals(LocalDate.of(2026, 10, 9), beforeMidnight.parse(fields("expense, 45, , today")).date());
        assertEquals(LocalDate.of(2026, 10, 8),
                beforeMidnight.parse(fields("expense, 45, , yesterday")).date());
        assertEquals(LocalDate.of(2026, 10, 10), atMidnight.parse(fields("expense, 45")).date());
        assertEquals(LocalDate.of(2026, 10, 9), atMidnight.parse(fields("expense, 45, , yesterday")).date());
    }

    private ExpenseParser parserAt(String instant) {
        return new ExpenseParser(dateParserAt(instant, BUSINESS_ZONE));
    }

    private static DateParser dateParserAt(String instant, ZoneId zoneId) {
        return new DateParser(Clock.fixed(Instant.parse(instant), zoneId));
    }

    private ExpenseCommandAssert parse(String message) {
        return new ExpenseCommandAssert(parser.parse(fields(message)));
    }

    private static List<String> fields(String message) {
        return Arrays.asList(message.split(",", -1));
    }

    private static final class ExpenseCommandAssert {
        private final com.asadaclan.accounting.entities.Expense command;

        private ExpenseCommandAssert(com.asadaclan.accounting.entities.Expense command) {
            this.command = command;
        }

        private ExpenseCommandAssert hasCost(String amount) {
            assertEquals(new BigDecimal(amount), command.cost());
            return this;
        }

        private ExpenseCommandAssert hasReason(String reason) {
            assertEquals(reason, command.reason());
            return this;
        }

        private ExpenseCommandAssert hasNoReason() {
            assertNull(command.reason());
            return this;
        }

        private ExpenseCommandAssert hasDate(LocalDate date) {
            assertEquals(date, command.date());
            return this;
        }
    }
}
