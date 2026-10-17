package com.asadaclan.accounting.services;

import org.springframework.stereotype.Service;

import com.asadaclan.accounting.repositories.ExpenseRepository;

import com.asadaclan.accounting.entities.Expense;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public Expense processExpense(Expense expense) {
        return expenseRepository.save(expense);
    }
}
