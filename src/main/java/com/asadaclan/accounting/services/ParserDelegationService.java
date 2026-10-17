package com.asadaclan.accounting.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.asadaclan.accounting.enums.MessageAction;
import com.asadaclan.accounting.parsers.ExpenseParser;
import com.asadaclan.accounting.parsers.HelpParser;
import com.asadaclan.accounting.parsers.PayoutParser;
// import com.asadaclan.accounting.parsers.QueryParser;
import com.asadaclan.accounting.parsers.ReportParser;
import com.asadaclan.accounting.parsers.TransactionParser;
import com.asadaclan.accounting.parsers.UndoParser;

@Service
public class ParserDelegationService {

    private final TransactionParser transactionParser;
    private final PayoutParser payoutParser;
    private final ExpenseParser expenseParser;
    // private final QueryParser queryParser;
    private final ReportParser reportParser;
    private final UndoParser undoParser;
    private final HelpParser helpParser;

    public ParserDelegationService(TransactionParser transactionParser, PayoutParser payoutParser,
            ExpenseParser expenseParser, /* QueryParser queryParser, */ ReportParser reportParser,
            UndoParser undoParser,
            HelpParser helpParser) {
        this.transactionParser = transactionParser;
        this.payoutParser = payoutParser;
        this.expenseParser = expenseParser;
        // this.queryParser = queryParser;
        this.reportParser = reportParser;
        this.undoParser = undoParser;
        this.helpParser = helpParser;
    }

    public Object delegate(List<String> messageParts, MessageAction action) {
        return switch (action) {
            case Transaction -> transactionParser.parse(messageParts);
            case Payout -> payoutParser.parse(messageParts);
            case Expense -> expenseParser.parse(messageParts);
            // case Query -> queryParser.parse(messageParts);
            case Report -> reportParser.parse(messageParts);
            case Undo -> undoParser.parse(messageParts);
            case Help -> helpParser.parse(messageParts);
            case Default -> throw new IllegalArgumentException("No parser is configured for action: " + action);
        };
    }
}
