package com.asadaclan.accounting.parsers;

import java.util.List;
import java.util.UUID;

public interface Parser<T> {
    T parse(List<String> messageParts, UUID businessId);

    default T parse(List<String> messageParts) {
        return parse(messageParts, null);
    }
}
