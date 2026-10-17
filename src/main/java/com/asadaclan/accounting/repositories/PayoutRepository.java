package com.asadaclan.accounting.repositories;

import java.util.UUID;

import com.asadaclan.accounting.entities.Payout;
import org.springframework.data.repository.CrudRepository;

public interface PayoutRepository extends CrudRepository<Payout, UUID> {
}
