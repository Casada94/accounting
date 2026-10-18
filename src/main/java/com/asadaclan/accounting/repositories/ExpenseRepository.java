package com.asadaclan.accounting.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.asadaclan.accounting.entities.Expense;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;

public interface ExpenseRepository extends CrudRepository<Expense, UUID> {
    @Query("""
            SELECT e.id, e.business_id, e.cost, e.reason, e.business_date
            FROM expenses e
            WHERE e.business_id = :businessId
              AND e.business_date BETWEEN :startDate AND :endDate
              AND NOT EXISTS (
                  SELECT 1
                  FROM `undos` u
                  WHERE u.business_id = e.business_id
                    AND u.entry_type = 'EXPENSE'
                    AND u.entry_id = e.id
              )
            ORDER BY e.business_date ASC
            """)
    List<Expense> findAllNotUndoneByBusinessIdAndDateBetweenOrderByDateAsc(
            @Param("businessId") String businessId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
