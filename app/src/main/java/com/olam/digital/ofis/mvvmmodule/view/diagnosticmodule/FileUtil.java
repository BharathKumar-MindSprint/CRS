package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule;

import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtil {

    public static String moveFile(String sourceFilePath, String destinationFolder) {
        File sourceFile = new File(sourceFilePath);
        File destinationDirectory = new File(destinationFolder);

        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdir();
        }

        File destinationFile = new File(destinationDirectory, sourceFile.getName());

        try {
            moveFile(sourceFile, destinationFile);
            System.out.println("File moved successfully.");
            return destinationFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("An error occurred: " + e.getMessage());
            return null;
        }
    }

    private static void moveFile(File source, File destination) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.move(source.toPath(), destination.toPath());
        }
    }
}
