package com.asadaclan.accounting.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.asadaclan.accounting.entities.Payout;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;

public interface PayoutRepository extends CrudRepository<Payout, UUID> {
    @Query("""
            SELECT p.id, p.business_id, p.amount, p.business_date
            FROM payouts p
            WHERE p.business_id = :businessId
              AND p.business_date BETWEEN :startDate AND :endDate
              AND NOT EXISTS (
                  SELECT 1
                  FROM `undos` u
                  WHERE u.business_id = p.business_id
                    AND u.entry_type = 'PAYOUT'
                    AND u.entry_id = p.id
              )
            ORDER BY p.business_date ASC
            """)
    List<Payout> findAllNotUndoneByBusinessIdAndDateBetweenOrderByDateAsc(
            @Param("businessId") String businessId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
