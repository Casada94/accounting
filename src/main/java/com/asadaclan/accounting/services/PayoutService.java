package com.asadaclan.accounting.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.asadaclan.accounting.entities.Payout;
import com.asadaclan.accounting.repositories.PayoutRepository;

@Service
public class PayoutService {

    private final PayoutRepository payoutRepository;

    public PayoutService(PayoutRepository payoutRepository) {
        this.payoutRepository = payoutRepository;
    }
    
    public Payout processPayout(Payout payout) {
        // Save the payout to the database
        Payout savedPayout = payoutRepository.save(payout);
        return savedPayout;
    }
}
