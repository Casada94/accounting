package com.asadaclan.accounting.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("businesses")
public final class Business {
    @Id
    @Column("phone_number")
    private final String phoneNumber;

    @Column("name")
    private final String name;

    private final String email;

    @PersistenceCreator
    public Business(String phoneNumber, String name, String email) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.email = email;
    }

    public String phoneNumber() {
        return phoneNumber;
    }

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }
}
