package com.asadaclan.accounting.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

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

    @Mock
    private BusinessRepository businessRepository;

    private BusinessAuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new BusinessAuthorizationService(businessRepository);
    }

    @Test
    void returnsTheBusinessIdForARegisteredPhoneNumber() {
        UUID businessId = UUID.randomUUID();
        when(businessRepository.findByPhoneNumber(PHONE_NUMBER))
                .thenReturn(Optional.of(new Business(businessId, PHONE_NUMBER)));

        assertEquals(businessId, service.requireBusinessId(PHONE_NUMBER));
    }

    @Test
    void trimsThePhoneNumberBeforeLookup() {
        UUID businessId = UUID.randomUUID();
        when(businessRepository.findByPhoneNumber(PHONE_NUMBER))
                .thenReturn(Optional.of(new Business(businessId, PHONE_NUMBER)));

        assertEquals(businessId, service.requireBusinessId("  " + PHONE_NUMBER + "  "));
        verify(businessRepository).findByPhoneNumber(PHONE_NUMBER);
    }

    @Test
    void rejectsAnUnregisteredPhoneNumber() {
        when(businessRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.empty());

        assertThrows(UnregisteredPhoneNumberException.class,
                () -> service.requireBusinessId(PHONE_NUMBER));
    }

    @Test
    void rejectsMissingPhoneNumberWithoutQueryingTheDatabase() {
        assertThrows(UnregisteredPhoneNumberException.class, () -> service.requireBusinessId(null));
        assertThrows(UnregisteredPhoneNumberException.class, () -> service.requireBusinessId("  "));

        verifyNoInteractions(businessRepository);
    }
}
