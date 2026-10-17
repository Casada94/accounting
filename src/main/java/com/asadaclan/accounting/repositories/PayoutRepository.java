package com.asadaclan.accounting.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.asadaclan.accounting.entities.Payout;
import org.springframework.data.repository.CrudRepository;

public interface PayoutRepository extends CrudRepository<Payout, UUID> {
    List<Payout> findAllByBusinessIdAndDateBetweenOrderByDateAsc(
            UUID businessId, LocalDate startDate, LocalDate endDate);
}
