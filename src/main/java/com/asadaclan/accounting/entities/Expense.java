package com.asadaclan.accounting.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("expense")
public final class Expense implements Persistable<UUID> {
    @Id
    private final UUID id;

    @Column("business_id")
    private final UUID businessId;

    private final BigDecimal cost;

    private final String reason;

    @Column("business_date")
    private final LocalDate date;

    public Expense(BigDecimal cost, String reason, LocalDate date) {
        this(null, null, cost, reason, date);
    }

    @PersistenceCreator
    public Expense(UUID id, UUID businessId, BigDecimal cost, String reason, LocalDate date) {
        this.id = id;
        this.businessId = businessId;
        this.cost = cost;
        this.reason = reason;
        this.date = date;
    }

    public Expense withId(UUID id) {
        return new Expense(id, businessId, cost, reason, date);
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

    public UUID businessId() {
        return businessId;
    }

    public BigDecimal cost() {
        return cost;
    }

    public String reason() {
        return reason;
    }

    public LocalDate date() {
        return date;
    }
}
