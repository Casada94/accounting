package com.asadaclan.accounting.entities;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("undo")
public final class Undo implements Persistable<UUID> {
    @Id
    private final UUID id;

    @Column("business_id")
    private final UUID businessId;

    @Column("transaction_id")
    private final UUID transactionId;

    public Undo(UUID transactionId) {
        this(null, null, transactionId);
    }

    @PersistenceCreator
    public Undo(UUID id, UUID businessId, UUID transactionId) {
        this.id = id;
        this.businessId = businessId;
        this.transactionId = transactionId;
    }

    public Undo withId(UUID id) {
        return new Undo(id, businessId, transactionId);
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

    public UUID transactionId() {
        return transactionId;
    }
}
