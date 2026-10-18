package com.asadaclan.accounting.entities;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.asadaclan.accounting.enums.LedgerEntryType;

@Table("undos")
public final class Undo implements Persistable<UUID> {
    @Id
    private final UUID id;

    @Column("business_id")
    private final String businessId;

    @Column("entry_type")
    private final LedgerEntryType entryType;

    @Column("entry_id")
    private final UUID entryId;

    public Undo(LedgerEntryType entryType, UUID entryId) {
        this(null, null, entryType, entryId);
    }

    @PersistenceCreator
    public Undo(UUID id, String businessId, LedgerEntryType entryType, UUID entryId) {
        this.id = id;
        this.businessId = businessId;
        this.entryType = entryType;
        this.entryId = entryId;
    }

    public Undo withId(UUID id) {
        return new Undo(id, businessId, entryType, entryId);
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

    public LedgerEntryType entryType() {
        return entryType;
    }

    public UUID entryId() {
        return entryId;
    }
}
