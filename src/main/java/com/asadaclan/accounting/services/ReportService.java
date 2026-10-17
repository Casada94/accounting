package com.asadaclan.accounting.services;

import org.springframework.stereotype.Service;

import com.asadaclan.accounting.repositories.ReportRepository;

import com.asadaclan.accounting.entities.Report;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public void generateReport(Report report) {
        reportRepository.save(report);
    }
}
