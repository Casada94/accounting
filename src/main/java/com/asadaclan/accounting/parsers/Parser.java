package com.asadaclan.accounting.parsers;

import java.util.List;

public interface Parser<T> {
    T parse(List<String> messageParts);
}
