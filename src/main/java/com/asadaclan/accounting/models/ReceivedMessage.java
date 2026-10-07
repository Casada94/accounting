package com.asadaclan.accounting.models;

import java.time.LocalDate;
import java.util.Optional;

import com.asadaclan.accounting.enums.MessageAction;

public record ReceivedMessage(
        MessageAction messageAction,
        Optional<String> transactionId,
        Optional<LocalDate> date,
        String items,
        Float transactionAmount,
        Optional<Float> tip) {
}
