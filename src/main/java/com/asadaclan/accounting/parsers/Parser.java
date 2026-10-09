package com.asadaclan.accounting.parsers;

interface Parser<T> {
    T parse(String[] parts);
}
