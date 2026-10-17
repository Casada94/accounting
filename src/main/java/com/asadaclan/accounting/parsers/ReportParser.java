package com.asadaclan.accounting.parsers;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.asadaclan.accounting.exceptions.CommandParseException;
import com.asadaclan.accounting.exceptions.ParseErrorCode;
import com.asadaclan.accounting.entities.Report;
import com.asadaclan.accounting.util.DateParser;

/**
 * Parses one {@code report, month, month?, year?} or {@code report, year, year?} command.
 */
@Component
public class ReportParser implements Parser<Report> {
    private final DateParser dateParser;

    public ReportParser(DateParser dateParser) {
        this.dateParser = dateParser;
    }

    @Override
    public Report parse(List<String> messageParts) {
        String[] parts = messageParts == null ? null : messageParts.toArray(String[]::new);
        if (parts == null || parts.length == 0) {
            throw new CommandParseException(
                    ParseErrorCode.EMPTY_MESSAGE,
                    0, "action", "Message must not be blank");
        }

        String[] fields = new String[parts.length];
        for (int index = 0; index < parts.length; index++) {
            String part = parts[index];
            if (part != null && (part.indexOf('\n') >= 0 || part.indexOf('\r') >= 0)) {
                throw new CommandParseException(
                        ParseErrorCode.WRONG_ARGUMENT_COUNT,
                        -1, null, "Message must contain exactly one command");
            }
            fields[index] = part == null ? "" : part.trim();
        }

        if (fields.length < 2 || fields[1].isEmpty()) {
            throw new CommandParseException(
                    ParseErrorCode.MISSING_REQUIRED_FIELD,
                    1, "period", "Required field is missing: period");
        }

        LocalDate businessDate = dateParser.businessDate();
        String reportType = fields[1].toLowerCase(Locale.ROOT);
        if (reportType.equals("month")) {
            if (fields.length > 4) {
                throw new CommandParseException(
                        ParseErrorCode.WRONG_ARGUMENT_COUNT,
                        -1, null, "Expected: report, month, month?, year?");
            }

            String rawMonth = fields.length > 2 ? fields[2] : "";
            String rawYear = fields.length > 3 ? fields[3] : "";
            int month = rawMonth.isEmpty()
                    ? businessDate.getMonthValue()
                    : parseMonth(rawMonth);
            Year year = rawYear.isEmpty()
                    ? Year.of(businessDate.getYear())
                    : parseYear(rawYear, 3);
            YearMonth period = YearMonth.of(year.getValue(), month);
            return new Report("month", Optional.of(period.toString()));
        }

        if (reportType.equals("year")) {
            if (fields.length > 3) {
                throw new CommandParseException(
                        ParseErrorCode.WRONG_ARGUMENT_COUNT,
                        -1, null, "Expected: report, year, year?");
            }

            String rawYear = fields.length > 2 ? fields[2] : "";
            Year year = rawYear.isEmpty()
                    ? Year.of(businessDate.getYear())
                    : parseYear(rawYear, 2);
            return new Report("year", Optional.of(year.toString()));
        }

        throw new CommandParseException(
                ParseErrorCode.INVALID_PERIOD,
                1, "period", "Invalid report period");
    }

    private int parseMonth(String raw) {
        try {
            return dateParser.parseMonth(raw);
        } catch (DateTimeParseException exception) {
            throw new CommandParseException(
                    ParseErrorCode.INVALID_MONTH,
                    2, "month", "Invalid month");
        }
    }

    private Year parseYear(String raw, int fieldIndex) {
        try {
            return dateParser.parseYear(raw);
        } catch (DateTimeParseException exception) {
            throw new CommandParseException(
                    ParseErrorCode.INVALID_YEAR,
                    fieldIndex, "year", "Invalid year");
        }
    }
}
