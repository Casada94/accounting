package com.asadaclan.accounting.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.asadaclan.accounting.entities.Undo;
import com.asadaclan.accounting.enums.LedgerEntryType;
import com.asadaclan.accounting.exceptions.CommandParseException;
import com.asadaclan.accounting.exceptions.ParseErrorCode;

class UndoParserTests {
    private UndoParser parser;

    @BeforeEach
    void setUp() {
        parser = new UndoParser();
    }

    @ParameterizedTest
    @CsvSource({
            "'undo, transaction, 3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e47', TRANSACTION, 3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e47",
            "'undo, expense, 6b2e1d40-0c5a-4f93-a871-9e4c3d2b7a16', EXPENSE, 6b2e1d40-0c5a-4f93-a871-9e4c3d2b7a16",
            "'undo, payout, 8a9c01f2-19ba-4a53-a166-5485c1b8f31b', PAYOUT, 8a9c01f2-19ba-4a53-a166-5485c1b8f31b",
            "'  UNDO ,  tattoo ,  3F1A6C2E-8B4D-4E7A-9C15-2D6F0A8B1E47  ', TRANSACTION, 3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e47",
            "'undo, jewelry, 00000000-0000-0000-0000-000000000000', TRANSACTION, 00000000-0000-0000-0000-000000000000"
    })
    void parsesLedgerEntryReferences(String input, LedgerEntryType expectedType, String expectedId) {
        Undo command = parser.parse(fields(input));

        assertEquals(expectedType, command.entryType());
        assertEquals(UUID.fromString(expectedId), command.entryId());
    }

    @ParameterizedTest
    @CsvSource(nullValues = "NULL", value = {
            "'undo', MISSING_REQUIRED_FIELD, 1, entryType",
            "'undo,', MISSING_REQUIRED_FIELD, 1, entryType",
            "'undo, transaction', MISSING_REQUIRED_FIELD, 2, entryId",
            "'undo, sale, 3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e47', INVALID_ENTRY_TYPE, 1, entryType",
            "'undo, expense, 1042', INVALID_ENTRY_ID, 2, entryId",
            "'undo, payout, 3f1a6c2e8b4d4e7a9c152d6f0a8b1e47', INVALID_ENTRY_ID, 2, entryId",
            "'undo, transaction, 3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e4g', INVALID_ENTRY_ID, 2, entryId",
            "'undo, transaction, 3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e47, extra', WRONG_ARGUMENT_COUNT, -1, NULL"
    })
    void returnsStructuredErrors(String input, ParseErrorCode expectedCode, int fieldIndex, String fieldName) {
        CommandParseException exception = assertThrows(CommandParseException.class,
                () -> parser.parse(fields(input)));

        assertEquals(expectedCode, exception.code());
        assertEquals(fieldIndex, exception.fieldIndex());
        assertEquals(fieldName, exception.fieldName());
    }

    @Test
    void rejectsBlankMessagesAndASecondCommand() {
        assertEquals(ParseErrorCode.EMPTY_MESSAGE,
                assertThrows(CommandParseException.class, () -> parser.parse(null)).code());
        assertEquals(ParseErrorCode.EMPTY_MESSAGE,
                assertThrows(CommandParseException.class, () -> parser.parse(List.of())).code());
        assertEquals(ParseErrorCode.MISSING_REQUIRED_FIELD,
                assertThrows(CommandParseException.class, () -> parser.parse(fields("   "))).code());
        assertEquals(ParseErrorCode.WRONG_ARGUMENT_COUNT,
                assertThrows(CommandParseException.class,
                        () -> parser.parse(fields("undo, transaction, 3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e47\n"
                                + "undo, expense, 6b2e1d40-0c5a-4f93-a871-9e4c3d2b7a16")))
                        .code());
    }

    private static List<String> fields(String message) {
        return Arrays.asList(message.split(",", -1));
    }
}
