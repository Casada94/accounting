package com.asadaclan.accounting.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.asadaclan.accounting.entities.Business;

public interface BusinessRepository extends CrudRepository<Business, UUID> {
    Optional<Business> findByPhoneNumber(String phoneNumber);
}
