package com.asadaclan.accounting.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("payouts")
public final class Payout implements Persistable<UUID> {
    @Id
    private final UUID id;

    @Column("business_id")
    private final String businessId;

    private final BigDecimal amount;

    @Column("business_date")
    private final LocalDate date;

    public Payout(BigDecimal amount, LocalDate date) {
        this(null, null, amount, date);
    }

    @PersistenceCreator
    public Payout(UUID id, String businessId, BigDecimal amount, LocalDate date) {
        this.id = id;
        this.businessId = businessId;
        this.amount = amount;
        this.date = date;
    }

    public Payout withId(UUID id) {
        return new Payout(id, businessId, amount, date);
    }

    @Override
    public boolean isNew() {
        return id == null;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public UUID id() {
        return id;
    }

    public String businessId() {
        return businessId;
    }

    public BigDecimal amount() {
        return amount;
    }

    public LocalDate date() {
        return date;
    }
}
