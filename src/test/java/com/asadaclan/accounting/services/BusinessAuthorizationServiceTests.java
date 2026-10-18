package com.asadaclan.accounting.services;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asadaclan.accounting.entities.Business;
import com.asadaclan.accounting.exceptions.UnregisteredPhoneNumberException;
import com.asadaclan.accounting.repositories.BusinessRepository;

@ExtendWith(MockitoExtension.class)
class BusinessAuthorizationServiceTests {
    private static final String PHONE_NUMBER = "+15551234567";
    private static final String BUSINESS_NAME = "Example Studio";
    private static final String EMAIL = "owner@example.com";

    @Mock
    private BusinessRepository businessRepository;

    private BusinessAuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new BusinessAuthorizationService(businessRepository);
    }

    @Test
    void returnsTheBusinessForARegisteredPhoneNumber() {
        Business business = new Business(PHONE_NUMBER, BUSINESS_NAME, EMAIL);
        when(businessRepository.findById(PHONE_NUMBER))
                .thenReturn(Optional.of(business));

        assertSame(business, service.requireBusiness(PHONE_NUMBER));
    }

    @Test
    void trimsThePhoneNumberBeforeLookup() {
        Business business = new Business(PHONE_NUMBER, BUSINESS_NAME, EMAIL);
        when(businessRepository.findById(PHONE_NUMBER))
                .thenReturn(Optional.of(business));

        assertSame(business, service.requireBusiness("  " + PHONE_NUMBER + "  "));
        verify(businessRepository).findById(PHONE_NUMBER);
    }

    @Test
    void rejectsAnUnregisteredPhoneNumber() {
        when(businessRepository.findById(PHONE_NUMBER)).thenReturn(Optional.empty());

        assertThrows(UnregisteredPhoneNumberException.class,
                () -> service.requireBusiness(PHONE_NUMBER));
    }

    @Test
    void rejectsMissingPhoneNumberWithoutQueryingTheDatabase() {
        assertThrows(UnregisteredPhoneNumberException.class, () -> service.requireBusiness(null));
        assertThrows(UnregisteredPhoneNumberException.class, () -> service.requireBusiness("  "));

        verifyNoInteractions(businessRepository);
    }
}
