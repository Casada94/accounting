package com.asadaclan.accounting.repositories;

import java.util.UUID;

import com.asadaclan.accounting.entities.Report;
import org.springframework.data.repository.CrudRepository;

public interface ReportRepository extends CrudRepository<Report, UUID> {
}
