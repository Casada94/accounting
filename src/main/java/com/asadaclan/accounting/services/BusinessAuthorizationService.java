package com.asadaclan.accounting.services;

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

    public Business requireBusiness(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new UnregisteredPhoneNumberException();
        }

        return businessRepository.findById(phoneNumber.trim())
                .orElseThrow(UnregisteredPhoneNumberException::new);
    }
}
