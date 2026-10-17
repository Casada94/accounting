package com.asadaclan.accounting.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.asadaclan.accounting.entities.Expense;
import org.springframework.data.repository.CrudRepository;

public interface ExpenseRepository extends CrudRepository<Expense, UUID> {
    List<Expense> findAllByBusinessIdAndDateBetweenOrderByDateAsc(
            UUID businessId, LocalDate startDate, LocalDate endDate);
}
