package com.asadaclan.accounting.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.asadaclan.accounting.exceptions.ProcessExecutionServiceException;

@Service
public class ProcessExecutionService {
    private static final Logger log = LoggerFactory.getLogger(ProcessExecutionService.class);

    private String workingDir;

    public ProcessExecutionService(@Value("${processExecution.workingDir.pdf}") String workingDir) {
        this.workingDir = workingDir;
        try {
            Files.createDirectories(Path.of(workingDir));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create PDF working directory: " + workingDir, e);
        }
    }

    public File generatePDF(File rawLatexFile, String outputBaseName)
            throws ProcessExecutionServiceException, InterruptedException {
        // Arguments must be separate elements in the list or array
        ProcessBuilder builder = new ProcessBuilder(
                "pdflatex",
                "-interaction=nonstopmode",
                "-halt-on-error",
                "--jobname=" + outputBaseName,
                "--output-directory=" + workingDir,
                rawLatexFile.getAbsolutePath());

        // Optional: Merge standard error with standard output
        builder.redirectErrorStream(true);

        Process process;
        try {
            process = builder.start();
        } catch (IOException e) {
            log.error("Failed to start latex process.", e);
            throw new ProcessExecutionServiceException("Failed to start latex process.", e);
        }

        IOException readFailure = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug(line);
            }
        } catch (IOException e) {
            log.error("Error reading output stream from latex process.", e);
            readFailure = e;
        }

        int exitCode = process.waitFor();
        if (readFailure != null) {
            throw new ProcessExecutionServiceException("Error reading output stream from latex process.", readFailure);
        }
        if (exitCode != 0) {
            log.error("PDF generation process exited with code: {}", exitCode);
            throw new ProcessExecutionServiceException("PDF generation process exited with code: " + exitCode);
        }

        return resolveOutputFile(outputBaseName);
    }

    File resolveOutputFile(String outputBaseName) {
        return Path.of(workingDir, outputBaseName + ".pdf").toFile();
    }

}
