package com.asadaclan.accounting.services;

import org.springframework.stereotype.Service;

import com.asadaclan.accounting.repositories.TransactionRepository;

import com.asadaclan.accounting.entities.Transaction;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction processTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
}
