package com.asadaclan.accounting.entities;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("report")
public final class Report implements Persistable<UUID> {
    @Id
    private final UUID id;

    @Column("business_id")
    private final UUID businessId;

    @Column("report_type")
    private final String reportType;

    @Column("report_period")
    private final String reportPeriod;

    public Report(String reportType, Optional<String> reportPeriod) {
        this(null, null, reportType, reportPeriod == null ? null : reportPeriod.orElse(null));
    }

    @PersistenceCreator
    public Report(UUID id, UUID businessId, String reportType, String reportPeriod) {
        this.id = id;
        this.businessId = businessId;
        this.reportType = reportType;
        this.reportPeriod = reportPeriod;
    }

    public Report withId(UUID id) {
        return new Report(id, businessId, reportType, reportPeriod);
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

    public String reportType() {
        return reportType;
    }

    public Optional<String> reportPeriod() {
        return Optional.ofNullable(reportPeriod);
    }
}
