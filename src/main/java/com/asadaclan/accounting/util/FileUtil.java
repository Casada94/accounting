package com.asadaclan.accounting.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.Resource;

import com.asadaclan.accounting.exceptions.FileUtilException;

public class FileUtil {

    public static String readString(Resource resource) throws FileUtilException {
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FileUtilException(e);
        }
    }

    public static File writeToFile(String contents) throws FileUtilException {
        try {
            Path tempFile = Files.createTempFile("financial-report-", ".tex");
            Files.writeString(tempFile, contents, StandardCharsets.UTF_8);
            return tempFile.toFile();
        } catch (IOException exception) {
            throw new FileUtilException(exception);
        }
    }

    public static void deleteIfExists(File file) {
        if (file != null) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException ignored) {
                file.deleteOnExit();
            }
        }
    }

}
