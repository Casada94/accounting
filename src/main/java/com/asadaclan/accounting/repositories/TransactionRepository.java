package com.asadaclan.accounting.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.asadaclan.accounting.entities.Transaction;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;

public interface TransactionRepository extends CrudRepository<Transaction, UUID> {
    @Query("""
            SELECT t.id, t.business_id, t.price, t.tip, t.name, t.business_date
            FROM transactions t
            WHERE t.business_id = :businessId
              AND t.business_date BETWEEN :startDate AND :endDate
              AND NOT EXISTS (
                  SELECT 1
                  FROM `undos` u
                  WHERE u.business_id = t.business_id
                    AND u.entry_type = 'TRANSACTION'
                    AND u.entry_id = t.id
              )
            ORDER BY t.business_date ASC
            """)
    List<Transaction> findAllNotUndoneByBusinessIdAndDateBetweenOrderByDateAsc(
            @Param("businessId") String businessId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
