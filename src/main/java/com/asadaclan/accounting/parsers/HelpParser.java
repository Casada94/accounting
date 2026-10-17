package com.asadaclan.accounting.parsers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.asadaclan.accounting.entities.Help;
import com.asadaclan.accounting.enums.MessageAction;
import com.asadaclan.accounting.exceptions.CommandParseException;
import com.asadaclan.accounting.exceptions.ParseErrorCode;

/**
 * Parses a {@code help, messageAction?} command.
 */
@Component
public final class HelpParser implements Parser<Help> {
    private static final int ACTION_INDEX = 0;
    private static final int HELP_TOPIC_INDEX = 1;
    private static final int MAX_FIELDS = 2;

    @Override
    public Help parse(List<String> messageParts) {
        String[] parts = messageParts == null ? null : messageParts.toArray(String[]::new);
        if (parts == null || parts.length == 0 || parts[0] == null || parts[0].isBlank()) {
            throw new CommandParseException(
                    ParseErrorCode.EMPTY_MESSAGE,
                    ACTION_INDEX,
                    "action",
                    "Message must not be blank");
        }

        String[] fields = new String[parts.length];
        for (int index = 0; index < parts.length; index++) {
            String part = parts[index];
            if (part != null && (part.indexOf('\n') >= 0 || part.indexOf('\r') >= 0)) {
                throw new CommandParseException(
                        ParseErrorCode.WRONG_ARGUMENT_COUNT,
                        -1,
                        null,
                        "Message must contain exactly one command");
            }
            fields[index] = part == null ? "" : part.trim();
        }

        if (fields.length > MAX_FIELDS) {
            throw new CommandParseException(
                    ParseErrorCode.WRONG_ARGUMENT_COUNT,
                    -1,
                    null,
                    "Expected: help, messageAction?");
        }

        if (fields.length == 1 || fields[HELP_TOPIC_INDEX].isEmpty()) {
            return new Help();
        }

        MessageAction action = Arrays.stream(MessageAction.values())
                .filter(candidate -> candidate.name().equalsIgnoreCase(fields[HELP_TOPIC_INDEX]))
                .findFirst()
                .orElseThrow(() -> new CommandParseException(
                        ParseErrorCode.UNKNOWN_ACTION,
                        HELP_TOPIC_INDEX,
                        "messageAction",
                        "Unknown help action: " + fields[HELP_TOPIC_INDEX]));

        if (action == MessageAction.Help || action == MessageAction.Default) {
            throw new CommandParseException(
                    ParseErrorCode.UNKNOWN_ACTION,
                    HELP_TOPIC_INDEX,
                    "messageAction",
                    "Unknown help action: " + fields[HELP_TOPIC_INDEX]);
        }

        return new Help(Optional.of(action));
    }
}
