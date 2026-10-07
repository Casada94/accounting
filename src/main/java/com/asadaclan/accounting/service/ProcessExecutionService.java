package com.asadaclan.accounting.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.asadaclan.accounting.exceptions.ProcessExecutionServiceException;

@Service
public class ProcessExecutionService {
    private String workingDir;

    public ProcessExecutionService(@Value("processExecution.workingDir.pdf") String workingDir) {
        this.workingDir = workingDir;
    }

    public File generatePDF(File rawLatexFile, String outputFileName)
            throws ProcessExecutionServiceException, InterruptedException {
        // Arguments must be separate elements in the list or array
        ProcessBuilder builder = new ProcessBuilder("pdflatex", rawLatexFile.getAbsolutePath(),
                "--jobname=" + outputFileName, "--output-directory=" + workingDir);

        // Optional: Merge standard error with standard output
        builder.redirectErrorStream(true);

        // Start the process
        Process process;
        try {
            process = builder.start();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new ProcessExecutionServiceException("Failed to start latex process.");
        }

        // Read the output from the process's input stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                // log.error(line);
            }
        } catch (IOException e) {
            // log.error("Error reading output stream from latex process.");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Wait for the process to complete and get the exit value
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new ProcessExecutionServiceException("PDF generattion process exited with code: " + exitCode);
        }

        return new File(workingDir + outputFileName + ".pdf");
    }

}
