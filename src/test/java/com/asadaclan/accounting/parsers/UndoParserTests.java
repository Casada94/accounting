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
            "'undo, 3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e47', 3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e47",
            "'undo, 6b2e1d40-0c5a-4f93-a871-9e4c3d2b7a16', 6b2e1d40-0c5a-4f93-a871-9e4c3d2b7a16",
            "'  UNDO ,  3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e47  ', 3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e47",
            "'undo,3F1A6C2E-8B4D-4E7A-9C15-2D6F0A8B1E47', 3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e47",
            "'undo, 00000000-0000-0000-0000-000000000000', 00000000-0000-0000-0000-000000000000"
    })
    void parsesTransactionIds(String input, String expectedId) {
        Undo command = parser.parse(fields(input));

        assertEquals(UUID.fromString(expectedId), command.transactionId());
    }

    @ParameterizedTest
    @CsvSource(nullValues = "NULL", value = {
            "'undo', MISSING_REQUIRED_FIELD, 1, transactionId",
            "'undo,', MISSING_REQUIRED_FIELD, 1, transactionId",
            "'undo, 1042', INVALID_TRANSACTION_ID, 1, transactionId",
            "'undo, 0', INVALID_TRANSACTION_ID, 1, transactionId",
            "'undo, 3f1a6c2e8b4d4e7a9c152d6f0a8b1e47', INVALID_TRANSACTION_ID, 1, transactionId",
            "'undo, 3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e4', INVALID_TRANSACTION_ID, 1, transactionId",
            "'undo, 3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e477', INVALID_TRANSACTION_ID, 1, transactionId",
            "'undo, {3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e47}', INVALID_TRANSACTION_ID, 1, transactionId",
            "'undo, 3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e4g', INVALID_TRANSACTION_ID, 1, transactionId",
            "'undo, 3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e47, extra', WRONG_ARGUMENT_COUNT, -1, NULL"
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
                        () -> parser.parse(fields("undo, 3f1a6c2e-8b4d-4e7a-9c15-2d6f0a8b1e47\nundo, 6b2e1d40-0c5a-4f93-a871-9e4c3d2b7a16")))
                        .code());
    }

    private static List<String> fields(String message) {
        return Arrays.asList(message.split(",", -1));
    }
}
