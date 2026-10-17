package com.asadaclan.accounting.repositories;

import java.util.UUID;

import com.asadaclan.accounting.entities.Expense;
import org.springframework.data.repository.CrudRepository;

public interface ExpenseRepository extends CrudRepository<Expense, UUID> {
}
