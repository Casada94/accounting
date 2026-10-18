package com.asadaclan.accounting.repositories;

import org.springframework.data.repository.CrudRepository;

import com.asadaclan.accounting.entities.Business;

public interface BusinessRepository extends CrudRepository<Business, String> {
}
