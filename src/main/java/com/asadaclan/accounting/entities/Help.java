package com.asadaclan.accounting.entities;

import java.util.Optional;

import com.asadaclan.accounting.enums.MessageAction;

/**
 * Represents a request for help with one action or all supported actions.
 */
public record Help(Optional<MessageAction> action) {
    public Help() {
        this(Optional.empty());
    }

    public Help {
        action = action == null ? Optional.empty() : action;
        if (action.filter(value -> value == MessageAction.Help || value == MessageAction.Default).isPresent()) {
            throw new IllegalArgumentException("Help action must identify a supported command");
        }
    }
}
