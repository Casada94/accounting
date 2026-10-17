package com.asadaclan.accounting.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.asadaclan.accounting.entities.Transaction;
import org.springframework.data.repository.CrudRepository;

public interface TransactionRepository extends CrudRepository<Transaction, UUID> {
    List<Transaction> findAllByBusinessIdAndDateBetweenOrderByDateAsc(
            UUID businessId, LocalDate startDate, LocalDate endDate);
}
