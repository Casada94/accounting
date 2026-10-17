package com.asadaclan.accounting.reporting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.asadaclan.accounting.exceptions.PdfGenerationException;

class LatexPdfCompilerTests {

    @TempDir
    Path tempDirectory;

    @Test
    void compilesToUniqueTemporaryFilesAndCleansWorkingArtifacts() throws Exception {
        Path command = command("""
                #!/bin/sh
                for argument in "$@"; do
                  case "$argument" in
                    --jobname=*) jobname="${argument#--jobname=}" ;;
                    --output-directory=*) output="${argument#--output-directory=}" ;;
                  esac
                done
                printf 'generated pdf' > "$output/$jobname.pdf"
                printf 'auxiliary data' > "$output/$jobname.aux"
                """);
        Path workingDirectory = Files.createDirectory(tempDirectory.resolve("work"));
        LatexPdfCompiler compiler = compiler(command, workingDirectory, Duration.ofSeconds(2));
        File latexFile = Files.writeString(tempDirectory.resolve("report.tex"), "report").toFile();

        File first = compiler.compile(latexFile, "MonthlyFinancialReport");
        File second = compiler.compile(latexFile, "MonthlyFinancialReport");

        try {
            assertThat(first).exists().hasContent("generated pdf");
            assertThat(second).exists().hasContent("generated pdf");
            assertThat(first).isNotEqualTo(second);
            try (var artifacts = Files.list(workingDirectory)) {
                assertThat(artifacts).isEmpty();
            }
        } finally {
            Files.deleteIfExists(first.toPath());
            Files.deleteIfExists(second.toPath());
        }
    }

    @Test
    void rejectsSuccessfulProcessThatDoesNotCreateAPdf() throws Exception {
        Path command = command("""
                #!/bin/sh
                exit 0
                """);
        LatexPdfCompiler compiler = compiler(command, createWorkingDirectory(), Duration.ofSeconds(2));
        File latexFile = Files.writeString(tempDirectory.resolve("report.tex"), "report").toFile();

        assertThatThrownBy(() -> compiler.compile(latexFile, "report"))
                .isInstanceOf(PdfGenerationException.class)
                .hasMessageContaining("without producing a nonempty PDF");
    }

    @Test
    void reportsNonzeroExitCode() throws Exception {
        Path command = command("""
                #!/bin/sh
                exit 7
                """);
        LatexPdfCompiler compiler = compiler(command, createWorkingDirectory(), Duration.ofSeconds(2));
        File latexFile = Files.writeString(tempDirectory.resolve("report.tex"), "report").toFile();

        assertThatThrownBy(() -> compiler.compile(latexFile, "report"))
                .isInstanceOf(PdfGenerationException.class)
                .hasMessageContaining("code: 7");
    }

    @Test
    void timesOutAndCleansWorkingArtifacts() throws Exception {
        Path command = command("""
                #!/bin/sh
                sleep 5
                """);
        Path workingDirectory = createWorkingDirectory();
        LatexPdfCompiler compiler = compiler(command, workingDirectory, Duration.ofMillis(50));
        File latexFile = Files.writeString(tempDirectory.resolve("report.tex"), "report").toFile();

        assertThatThrownBy(() -> compiler.compile(latexFile, "report"))
                .isInstanceOf(PdfGenerationException.class)
                .hasMessageContaining("timed out");
        try (var artifacts = Files.list(workingDirectory)) {
            assertThat(artifacts).isEmpty();
        }
    }

    private LatexPdfCompiler compiler(Path command, Path workingDirectory, Duration timeout) {
        return new LatexPdfCompiler(command.toString(), workingDirectory.toString(), timeout);
    }

    private Path createWorkingDirectory() throws Exception {
        return Files.createTempDirectory(tempDirectory, "work-");
    }

    private Path command(String contents) throws Exception {
        Path command = tempDirectory.resolve("pdflatex-" + System.nanoTime());
        Files.writeString(command, contents, StandardCharsets.UTF_8);
        assertThat(command.toFile().setExecutable(true)).isTrue();
        return command;
    }
}
