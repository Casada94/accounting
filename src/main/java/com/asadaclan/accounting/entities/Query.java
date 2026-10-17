/* package com.asadaclan.accounting.entities;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("account_query")
public final class Query implements Persistable<UUID> {
    @Id
    private final UUID id;

    @Column("business_id")
    private final UUID businessId;

    @Column("query_type")
    private final String queryType;

    private final String[] args;

    public Query(String queryType) {
        this(null, null, queryType, null);
    }

    public Query(String queryType, String[] args) {
        this(null, null, queryType, args);
    }

    @PersistenceCreator
    public Query(UUID id, UUID businessId, String queryType, String[] args) {
        this.id = id;
        this.businessId = businessId;
        this.queryType = queryType;
        this.args = args == null ? null : args.clone();
    }

    public Query withId(UUID id) {
        return new Query(id, businessId, queryType, args);
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

    public String queryType() {
        return queryType;
    }

    public String[] args() {
        return args == null ? null : args.clone();
    }
}
*/
