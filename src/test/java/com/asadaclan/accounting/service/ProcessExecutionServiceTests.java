package com.asadaclan.accounting.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProcessExecutionServiceTests {

    @TempDir
    Path workingDirectory;

    @Test
    void resolvesPdfUsingBaseNameAndWorkingDirectory() {
        ProcessExecutionService service = new ProcessExecutionService(workingDirectory.toString());

        assertThat(service.resolveOutputFile("10_2026_MonthlyFinancialReport"))
                .isEqualTo(workingDirectory.resolve("10_2026_MonthlyFinancialReport.pdf").toFile());
    }
}
