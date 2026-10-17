package com.asadaclan.accounting.reporting;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.asadaclan.accounting.exceptions.PdfGenerationException;

@Component
public class LatexPdfCompiler {
    private static final Logger log = LoggerFactory.getLogger(LatexPdfCompiler.class);
    private static final Duration PROCESS_TERMINATION_GRACE_PERIOD = Duration.ofSeconds(5);

    private final String command;
    private final Path workingDirectory;
    private final Duration timeout;

    public LatexPdfCompiler(
            @Value("${reporting.pdf.command:pdflatex}") String command,
            @Value("${reporting.pdf.working-dir}") String workingDirectory,
            @Value("${reporting.pdf.timeout:60s}") Duration timeout) {
        this.command = command;
        this.workingDirectory = Path.of(workingDirectory);
        this.timeout = timeout;

        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("PDF generation timeout must be positive");
        }

        try {
            Files.createDirectories(this.workingDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to create PDF working directory: " + this.workingDirectory, exception);
        }
    }

    public File compile(File latexFile, String outputBaseName)
            throws PdfGenerationException, InterruptedException {
        validateOutputBaseName(outputBaseName);

        Path invocationDirectory = createInvocationDirectory();
        Path processLog = invocationDirectory.resolve("pdflatex.log");
        Path generatedPdf = invocationDirectory.resolve(outputBaseName + ".pdf");
        Process process = null;

        try {
            ProcessBuilder builder = new ProcessBuilder(
                    command,
                    "-interaction=nonstopmode",
                    "-halt-on-error",
                    "--jobname=" + outputBaseName,
                    "--output-directory=" + invocationDirectory,
                    latexFile.getAbsolutePath());
            builder.redirectErrorStream(true);
            builder.redirectOutput(processLog.toFile());

            try {
                process = builder.start();
            } catch (IOException exception) {
                throw new PdfGenerationException("Failed to start PDF generation process", exception);
            }

            boolean completed = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!completed) {
                stop(process);
                throw new PdfGenerationException(
                        "PDF generation timed out after " + timeout.toSeconds() + " seconds");
            }

            if (process.exitValue() != 0) {
                throw new PdfGenerationException(
                        "PDF generation process exited with code: " + process.exitValue());
            }
            if (!Files.isRegularFile(generatedPdf) || fileIsEmpty(generatedPdf)) {
                throw new PdfGenerationException("PDF generation completed without producing a nonempty PDF");
            }

            Path result = Files.createTempFile(outputBaseName + "-", ".pdf");
            Files.move(generatedPdf, result, StandardCopyOption.REPLACE_EXISTING);
            return result.toFile();
        } catch (InterruptedException exception) {
            if (process != null) {
                stop(process);
            }
            throw exception;
        } catch (IOException exception) {
            throw new PdfGenerationException("Unable to prepare PDF output", exception);
        } finally {
            deleteDirectory(invocationDirectory);
        }
    }

    private Path createInvocationDirectory() throws PdfGenerationException {
        try {
            return Files.createTempDirectory(workingDirectory, "latex-");
        } catch (IOException exception) {
            throw new PdfGenerationException("Unable to create PDF generation directory", exception);
        }
    }

    private boolean fileIsEmpty(Path file) throws IOException {
        return Files.size(file) == 0;
    }

    private void validateOutputBaseName(String outputBaseName) {
        if (outputBaseName == null || !outputBaseName.matches("[A-Za-z0-9._-]+")) {
            throw new IllegalArgumentException("PDF output name contains unsupported characters");
        }
    }

    private void stop(Process process) {
        List<ProcessHandle> descendants = process.descendants().toList();
        descendants.forEach(ProcessHandle::destroy);
        process.destroy();
        try {
            if (!process.waitFor(PROCESS_TERMINATION_GRACE_PERIOD.toMillis(), TimeUnit.MILLISECONDS)) {
                descendants.forEach(ProcessHandle::destroyForcibly);
                process.destroyForcibly();
            }
        } catch (InterruptedException exception) {
            descendants.forEach(ProcessHandle::destroyForcibly);
            process.destroyForcibly();
            Thread.currentThread().interrupt();
        }
        descendants.stream().filter(ProcessHandle::isAlive).forEach(ProcessHandle::destroyForcibly);
    }

    private void deleteDirectory(Path directory) {
        try (var paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    log.warn("Unable to delete temporary PDF artifact: {}", path, exception);
                }
            });
        } catch (IOException exception) {
            log.warn("Unable to clean temporary PDF directory: {}", directory, exception);
        }
    }
}
