package com.asadaclan.accounting.repositories;

import java.util.UUID;

import com.asadaclan.accounting.entities.Transaction;
import org.springframework.data.repository.CrudRepository;

public interface TransactionRepository extends CrudRepository<Transaction, UUID> {
}
