package com.mustapha.revisia.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileUtil {

    // Directory where uploaded files will be stored
    private static final String UPLOAD_DIR = System.getProperty("user.home") + File.separator + "RevisIA" + File.separator + "uploads";

    // Initialize directory
    static {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Created upload directory at: " + UPLOAD_DIR);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Impossible de créer le répertoire d'upload!", e);
        }
    }

    // Copy a file to the upload directory and return the new path
    public static String saveFile(File sourceFile, String userId) throws IOException {
        // Create user-specific directory to avoid filename conflicts
        String userDir = UPLOAD_DIR + File.separator + userId;
        Path userPath = Paths.get(userDir);
        if (!Files.exists(userPath)) {
            Files.createDirectories(userPath);
        }

        // Create destination file path
        String fileName = sourceFile.getName();
        String destPath = userDir + File.separator + fileName;

        // If file with same name exists, append timestamp to make it unique
        File destFile = new File(destPath);
        if (destFile.exists()) {
            String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));
            destPath = userDir + File.separator + nameWithoutExt + "_" + System.currentTimeMillis() + extension;
            destFile = new File(destPath);
        }

        // Copy the file
        Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("File saved to: " + destPath);

        return destPath;
    }

    // Delete a file
    public static boolean deleteFile(String filePath) {
        try {
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get a File object from a path
    public static File getFile(String filePath) {
        return new File(filePath);
    }
}