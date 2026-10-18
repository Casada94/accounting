package com.asadaclan.accounting.parsers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Component;

import com.asadaclan.accounting.exceptions.CommandParseException;
import com.asadaclan.accounting.exceptions.ParseErrorCode;
import com.asadaclan.accounting.entities.Payout;
import com.asadaclan.accounting.util.DateParser;

/**
 * Parses one {@code payout, amount, date?} command from comma-separated fields.
 * Parsing does not classify or record the payout.
 */
@Component
public final class PayoutParser implements Parser<Payout> {
    private static final String MONEY_PATTERN = "\\$?[0-9]+(?:\\.[0-9]{1,2})?";
    private static final int ACTION_INDEX = 0;
    private static final int AMOUNT_INDEX = 1;
    private static final int DATE_INDEX = 2;
    private static final int MAX_FIELDS = 3;

    private final DateParser dateParser;

    public PayoutParser(DateParser dateParser) {
        this.dateParser = dateParser;
    }

    @Override
    public Payout parse(List<String> messageParts, String businessId) {
        String[] parts = messageParts == null ? null : messageParts.toArray(String[]::new);
        if (parts == null || parts.length == 0) {
            throw error(ParseErrorCode.EMPTY_MESSAGE, ACTION_INDEX, "action", "Message must not be blank");
        }

        String[] fields = normalize(parts);
        if (fields.length > MAX_FIELDS) {
            throw error(ParseErrorCode.WRONG_ARGUMENT_COUNT, -1, null, "Expected: payout, amount, date?");
        }

        BigDecimal amount = parseAmount(required(fields, AMOUNT_INDEX, "amount"));
        LocalDate date = fields.length > DATE_INDEX && !fields[DATE_INDEX].isEmpty()
                ? parseDate(fields[DATE_INDEX])
                : dateParser.businessDate();
        return new Payout(null, businessId, amount, date);
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

    private String required(String[] fields, int index, String name) {
        if (index >= fields.length || fields[index].isEmpty()) {
            throw error(ParseErrorCode.MISSING_REQUIRED_FIELD, index, name,
                    "Required field is missing: " + name);
        }
        return fields[index];
    }

    private BigDecimal parseAmount(String raw) {
        if (!raw.matches(MONEY_PATTERN)) {
            throw invalidAmount("Invalid monetary amount");
        }
        try {
            BigDecimal amount = new BigDecimal(raw.charAt(0) == '$' ? raw.substring(1) : raw)
                    .setScale(2, RoundingMode.UNNECESSARY);
            if (amount.signum() <= 0) {
                throw invalidAmount("Amount must be positive");
            }
            return amount;
        } catch (NumberFormatException | ArithmeticException exception) {
            throw invalidAmount("Invalid monetary amount");
        }
    }

    private LocalDate parseDate(String raw) {
        try {
            return dateParser.parseTransactionDate(raw);
        } catch (DateTimeParseException exception) {
            throw error(ParseErrorCode.INVALID_DATE, DATE_INDEX, "date", "Invalid date");
        }
    }

    private CommandParseException invalidAmount(String message) {
        return error(ParseErrorCode.INVALID_AMOUNT, AMOUNT_INDEX, "amount", message);
    }

    private CommandParseException error(ParseErrorCode code, int fieldIndex, String fieldName, String message) {
        return new CommandParseException(code, fieldIndex, fieldName, message);
    }
}
