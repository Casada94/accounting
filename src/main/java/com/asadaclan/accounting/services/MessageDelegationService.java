package com.asadaclan.accounting.services;

import org.springframework.stereotype.Service;

import com.asadaclan.accounting.entities.Expense;
import com.asadaclan.accounting.entities.Help;
import com.asadaclan.accounting.entities.Payout;
// import com.asadaclan.accounting.entities.Query;
import com.asadaclan.accounting.entities.Report;
import com.asadaclan.accounting.entities.Transaction;
import com.asadaclan.accounting.entities.Undo;

@Service
public class MessageDelegationService {

    private final TransactionService transactionService;
    private final ExpenseService expenseService;
    private final PayoutService payoutService;
    // private final QueryService queryService;
    private final ReportService reportService;
    private final UndoService undoService;
    private final HelpService helpService;

    public MessageDelegationService(TransactionService transactionService, PayoutService payoutService,
            ExpenseService expenseService, /* QueryService queryService, */ ReportService reportService,
            UndoService undoService, HelpService helpService) {
        this.transactionService = transactionService;
        this.expenseService = expenseService;
        this.payoutService = payoutService;
        // this.queryService = queryService;
        this.reportService = reportService;
        this.undoService = undoService;
        this.helpService = helpService;
    }

    public String delegate(Transaction transaction) {
        return "transactionId: " + transactionService.processTransaction(transaction).getId();
    }

    public String delegate(Expense expense) {
        return "transactionId: " + expenseService.processExpense(expense).getId();
    }

    public String delegate(Payout payout) {
        return "transactionId: " + payoutService.processPayout(payout).getId();
    }

    // public String delegate(Query query) {
    //     return queryService.processQuery(query);
    // }

    public void delegate(Report report) {
        reportService.generateReport(report);
    }

    public String delegate(Undo undo) {
        return "Undo(" + undoService.undo(undo).getId()
                + ") Successfully undone transaction with ID: " + undo.transactionId();
    }

    public String delegate(Help help) {
        return helpService.processHelp(help);
    }
}
