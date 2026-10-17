package com.asadaclan.accounting.entities;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("businesses")
public final class Business {
    @Id
    private final UUID id;

    @Column("phone_number")
    private final String phoneNumber;

    @PersistenceCreator
    public Business(UUID id, String phoneNumber) {
        this.id = id;
        this.phoneNumber = phoneNumber;
    }

    public UUID id() {
        return id;
    }

    public String phoneNumber() {
        return phoneNumber;
    }
}
