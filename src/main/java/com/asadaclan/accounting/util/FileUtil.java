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

    public static File writeToFile(String contents) throws FileUtilException{
        Path tempFile;
        try {
            tempFile = Files.createTempFile("", "");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new FileUtilException();
        }
        try {
            tempFile = Files.write(tempFile, contents.getBytes());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new FileUtilException("Unable to Write Contents to file");
        }
        return tempFile.toFile();
    }

}
