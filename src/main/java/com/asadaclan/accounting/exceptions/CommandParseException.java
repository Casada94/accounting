package com.asadaclan.accounting.exceptions;

public final class CommandParseException extends IllegalArgumentException {
    private final ParseErrorCode code;
    private final int fieldIndex;
    private final String fieldName;

    public CommandParseException(ParseErrorCode code, int fieldIndex, String fieldName, String message) {
        super(message);
        this.code = code;
        this.fieldIndex = fieldIndex;
        this.fieldName = fieldName;
    }

    public ParseErrorCode code() {
        return code;
    }

    public int fieldIndex() {
        return fieldIndex;
    }

    public String fieldName() {
        return fieldName;
    }
}
