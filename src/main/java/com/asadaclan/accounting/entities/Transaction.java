package com.asadaclan.accounting.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("transactions")
public final class Transaction implements Persistable<UUID> {
    @Id
    private final UUID id;

    @Column("business_id")
    private final String businessId;

    private final BigDecimal price;

    private final BigDecimal tip;

    private final String name;

    @Column("business_date")
    private final LocalDate date;

    public Transaction(BigDecimal price, BigDecimal tip, String name, LocalDate date) {
        this(null, null, price, tip, name, date);
    }

    @PersistenceCreator
    public Transaction(UUID id, String businessId, BigDecimal price, BigDecimal tip, String name, LocalDate date) {
        this.id = id;
        this.businessId = businessId;
        this.price = price;
        this.tip = tip;
        this.name = name;
        this.date = date;
    }

    public Transaction withId(UUID id) {
        return new Transaction(id, businessId, price, tip, name, date);
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

    public BigDecimal price() {
        return price;
    }

    public BigDecimal tip() {
        return tip;
    }

    public String name() {
        return name;
    }

    public LocalDate date() {
        return date;
    }
}
