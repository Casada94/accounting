package com.asadaclan.accounting.entities;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("reports")
public final class ReportRequest implements Persistable<UUID> {
    @Id
    private final UUID id;

    @Column("business_id")
    private final String businessId;

    @Column("start_date")
    private final LocalDate startDate;

    @Column("end_date")
    private final LocalDate endDate;

    public ReportRequest(LocalDate startDate, LocalDate endDate) {
        this(null, null, startDate, endDate);
    }

    @PersistenceCreator
    public ReportRequest(UUID id, String businessId, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.businessId = businessId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public ReportRequest withId(UUID id) {
        return new ReportRequest(id, businessId, startDate, endDate);
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

    public LocalDate startDate() {
        return startDate;
    }

    public LocalDate endDate() {
        return endDate;
    }
}
