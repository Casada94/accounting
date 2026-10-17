package com.asadaclan.accounting.parsers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.asadaclan.accounting.entities.Help;
import com.asadaclan.accounting.enums.MessageAction;
import com.asadaclan.accounting.exceptions.CommandParseException;
import com.asadaclan.accounting.exceptions.ParseErrorCode;

class HelpParserTests {
    private final HelpParser parser = new HelpParser();

    @Test
    void parsesHelpCommand() {
        assertThat(parser.parse(List.of(" Help "))).isEqualTo(new Help());
    }

    @Test
    void parsesOptionalMessageActionCaseInsensitively() {
        assertThat(parser.parse(List.of("Help", " report ")))
                .isEqualTo(new Help(Optional.of(MessageAction.Report)));
    }

    @Test
    void treatsBlankOptionalMessageActionAsAbsent() {
        assertThat(parser.parse(List.of("Help", " "))).isEqualTo(new Help());
    }

    @Test
    void rejectsUnknownMessageAction() {
        assertThatThrownBy(() -> parser.parse(List.of("Help", "Unknown")))
                .isInstanceOfSatisfying(CommandParseException.class,
                        exception -> assertThat(exception.code()).isEqualTo(ParseErrorCode.UNKNOWN_ACTION));
    }

    @Test
    void rejectsHelpAsItsOwnTopic() {
        assertThatThrownBy(() -> parser.parse(List.of("Help", "Help")))
                .isInstanceOfSatisfying(CommandParseException.class,
                        exception -> assertThat(exception.code()).isEqualTo(ParseErrorCode.UNKNOWN_ACTION));
    }

    @Test
    void rejectsTooManyArguments() {
        assertThatThrownBy(() -> parser.parse(List.of("Help", "Report", "extra")))
                .isInstanceOfSatisfying(CommandParseException.class,
                        exception -> assertThat(exception.code()).isEqualTo(ParseErrorCode.WRONG_ARGUMENT_COUNT));
    }

    @Test
    void rejectsBlankMessage() {
        assertThatThrownBy(() -> parser.parse(List.of(" ")))
                .isInstanceOfSatisfying(CommandParseException.class,
                        exception -> assertThat(exception.code()).isEqualTo(ParseErrorCode.EMPTY_MESSAGE));
    }
}
