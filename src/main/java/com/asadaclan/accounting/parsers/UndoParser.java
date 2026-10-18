package com.asadaclan.accounting.parsers;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.asadaclan.accounting.exceptions.CommandParseException;
import com.asadaclan.accounting.exceptions.ParseErrorCode;
import com.asadaclan.accounting.entities.Undo;
import com.asadaclan.accounting.enums.LedgerEntryType;

/**
 * Parses one {@code undo, entryType, entryId} command from comma-separated fields.
 * Parsing validates the type and id syntax only. It does not look up, reverse, or delete an entry.
 */
@Component
public final class UndoParser implements Parser<Undo> {
    private static final String ID_PATTERN =
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    private static final int ACTION_INDEX = 0;
    private static final int ENTRY_TYPE_INDEX = 1;
    private static final int ENTRY_ID_INDEX = 2;
    private static final int MAX_FIELDS = 3;

    @Override
    public Undo parse(List<String> messageParts, String businessId) {
        String[] parts = messageParts == null ? null : messageParts.toArray(String[]::new);
        if (parts == null || parts.length == 0) {
            throw error(ParseErrorCode.EMPTY_MESSAGE, ACTION_INDEX, "action", "Message must not be blank");
        }

        String[] fields = normalize(parts);
        if (fields.length > MAX_FIELDS) {
            throw error(ParseErrorCode.WRONG_ARGUMENT_COUNT, -1, null,
                    "Expected: undo, entryType, entryId");
        }

        LedgerEntryType entryType = parseEntryType(required(fields, ENTRY_TYPE_INDEX, "entryType"));
        UUID entryId = parseEntryId(required(fields, ENTRY_ID_INDEX, "entryId"));
        return new Undo(null, businessId, entryType, entryId);
    }

    private String[] normalize(String[] parts) {
        String[] fields = new String[parts.length];
        for (int index = 0; index < parts.length; index++) {
            String part = parts[index];
            if (part != null && (part.indexOf('\n') >= 0 || part.indexOf('\r') >= 0)) {
                throw error(ParseErrorCode.WRONG_ARGUMENT_COUNT, -1, null,
                        "Message must contain exactly one command");
            }
            fields[index] = part == null ? "" : part.trim();
        }
        return fields;
    }

    private LedgerEntryType parseEntryType(String raw) {
        try {
            return LedgerEntryType.from(raw);
        } catch (IllegalArgumentException exception) {
            throw error(ParseErrorCode.INVALID_ENTRY_TYPE, ENTRY_TYPE_INDEX, "entryType",
                    "Invalid ledger entry type");
        }
    }

    private UUID parseEntryId(String raw) {
        if (!raw.matches(ID_PATTERN)) {
            throw invalidEntryId();
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException exception) {
            throw invalidEntryId();
        }
    }

    private String required(String[] fields, int index, String name) {
        if (index >= fields.length || fields[index].isEmpty()) {
            throw error(ParseErrorCode.MISSING_REQUIRED_FIELD, index, name,
                    "Required field is missing: " + name);
        }
        return fields[index];
    }

    private CommandParseException invalidEntryId() {
        return error(ParseErrorCode.INVALID_ENTRY_ID, ENTRY_ID_INDEX, "entryId",
                "Invalid ledger entry ID");
    }

    private CommandParseException error(ParseErrorCode code, int fieldIndex, String fieldName, String message) {
        return new CommandParseException(code, fieldIndex, fieldName, message);
    }
}
