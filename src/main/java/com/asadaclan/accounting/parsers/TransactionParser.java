package com.asadaclan.accounting.parsers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.asadaclan.accounting.exceptions.CommandParseException;
import com.asadaclan.accounting.exceptions.ParseErrorCode;
import com.asadaclan.accounting.entities.Transaction;
import com.asadaclan.accounting.util.DateParser;

/**
 * Parses one {@code tattoo, price, tip, name?, date?} command from comma-separated fields.
 * Jewelry uses the same fields and also maps to {@link Transaction}.
 * Parsing does not record the transaction or allocate a transaction id.
 */
@Component
public final class TransactionParser implements Parser<Transaction> {
    private static final String MONEY_PATTERN = "\\$?[0-9]+(?:\\.[0-9]{1,2})?";
    private static final int ACTION_INDEX = 0;
    private static final int PRICE_INDEX = 1;
    private static final int TIP_INDEX = 2;
    private static final int NAME_INDEX = 3;
    private static final int DATE_INDEX = 4;
    private static final int MAX_FIELDS = 5;

    private final DateParser dateParser;

    public TransactionParser(DateParser dateParser) {
        this.dateParser = dateParser;
    }

    @Override
    public Transaction parse(List<String> messageParts, UUID businessId) {
        String[] parts = messageParts == null ? null : messageParts.toArray(String[]::new);
        if (parts == null || parts.length == 0) {
            throw error(ParseErrorCode.EMPTY_MESSAGE, ACTION_INDEX, "action", "Message must not be blank");
        }

        String[] fields = normalize(parts);
        if (fields.length > MAX_FIELDS) {
            throw error(ParseErrorCode.WRONG_ARGUMENT_COUNT, -1, null,
                    "Expected: tattoo, price, tip, name?, date?");
        }

        BigDecimal price = parseMoney(required(fields, PRICE_INDEX, "price"), PRICE_INDEX, "price", true);
        BigDecimal tip = parseMoney(required(fields, TIP_INDEX, "tip"), TIP_INDEX, "tip", false);
        String name = optionalText(fields, NAME_INDEX);
        LocalDate date = fields.length > DATE_INDEX && !fields[DATE_INDEX].isEmpty()
                ? parseDate(fields[DATE_INDEX])
                : dateParser.businessDate();
        return new Transaction(null, businessId, price, tip, name, date);
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

    private BigDecimal parseMoney(String raw, int index, String name, boolean strictlyPositive) {
        if (!raw.matches(MONEY_PATTERN)) {
            throw error(ParseErrorCode.INVALID_AMOUNT, index, name, "Invalid monetary amount");
        }
        try {
            BigDecimal amount = new BigDecimal(raw.charAt(0) == '$' ? raw.substring(1) : raw)
                    .setScale(2, RoundingMode.UNNECESSARY);
            boolean invalidSign = strictlyPositive ? amount.signum() <= 0 : amount.signum() < 0;
            if (invalidSign) {
                throw error(ParseErrorCode.INVALID_AMOUNT, index, name,
                        strictlyPositive ? "Amount must be positive" : "Amount must be nonnegative");
            }
            return amount;
        } catch (NumberFormatException | ArithmeticException exception) {
            throw error(ParseErrorCode.INVALID_AMOUNT, index, name, "Invalid monetary amount");
        }
    }

    private LocalDate parseDate(String raw) {
        try {
            return dateParser.parseTransactionDate(raw);
        } catch (DateTimeParseException exception) {
            throw invalidDate();
        }
    }

    private String required(String[] fields, int index, String name) {
        if (index >= fields.length || fields[index].isEmpty()) {
            throw error(ParseErrorCode.MISSING_REQUIRED_FIELD, index, name,
                    "Required field is missing: " + name);
        }
        return fields[index];
    }

    private String optionalText(String[] fields, int index) {
        return fields.length > index && !fields[index].isEmpty() ? fields[index] : null;
    }

    private CommandParseException invalidDate() {
        return error(ParseErrorCode.INVALID_DATE, DATE_INDEX, "date", "Invalid date");
    }

    private CommandParseException error(ParseErrorCode code, int fieldIndex, String fieldName, String message) {
        return new CommandParseException(code, fieldIndex, fieldName, message);
    }
}
