package com.example.pr_1_file_dupe.service;

import com.example.pr_1_file_dupe.FileData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileScanner {

    public List<FileData> scanDirectory(String path) {

        List<FileData> fileList = new ArrayList<>();

        File root = new File(path);

        // Check if path exists
        if (!root.exists()) {
            System.out.println("Path does not exist!");
            return fileList;
        }

        File[] files = root.listFiles();

        // Important null check
        if (files == null) {
            return fileList;
        }

        for (File file : files) {

            if (file.isDirectory()) {
                // 🔁 Recursion
                fileList.addAll(scanDirectory(file.getAbsolutePath()));
            } else {
                // 📄 Process file

                String name = file.getName();
                long size = file.length();
                String fullPath = file.getAbsolutePath();

                // Extract file type (extension)
                String type = getFileExtension(name);

                FileData fileData = new FileData(name, type, size, fullPath);

                fileList.add(fileData);
            }
        }

        return fileList;
    }

    // Helper method for file extension
    private String getFileExtension(String fileName) {

        int lastDotIndex = fileName.lastIndexOf('.');

        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "unknown";
        }

        return fileName.substring(lastDotIndex + 1);
    }
}
