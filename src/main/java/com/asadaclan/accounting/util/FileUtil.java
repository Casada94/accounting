package com.asadaclan.accounting.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.asadaclan.accounting.exceptions.FileUtilException;

public class FileUtil {

    public static String readString(File file) throws FileUtilException {
        String contents;
        try {
            contents = Files.readString(Paths.get(file.getPath()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new FileUtilException();
        }

        return contents;
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
