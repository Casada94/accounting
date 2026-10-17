package com.asadaclan.accounting.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.asadaclan.accounting.entities.Business;
import com.asadaclan.accounting.exceptions.UnregisteredPhoneNumberException;
import com.asadaclan.accounting.repositories.BusinessRepository;

@Service
public class BusinessAuthorizationService {
    private final BusinessRepository businessRepository;

    public BusinessAuthorizationService(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    public UUID requireBusinessId(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new UnregisteredPhoneNumberException();
        }

        return businessRepository.findByPhoneNumber(phoneNumber.trim())
                .map(Business::id)
                .orElseThrow(UnregisteredPhoneNumberException::new);
    }
}
