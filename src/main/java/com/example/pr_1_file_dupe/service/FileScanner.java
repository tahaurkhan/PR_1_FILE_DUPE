package com.example.pr_1_file_dupe.service;

import com.example.pr_1_file_dupe.DataStore;
import com.example.pr_1_file_dupe.FileData;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileScanner {

    // Variable to track skipped files
    private int skippedCount = 0;

    public List<FileData> scanDirectory(String path) {
        List<FileData> fileList = new ArrayList<>();
        File root = new File(path);

        if (!root.exists()) {
            System.out.println("Path does not exist!");
            return fileList;
        }

        // 1. Load User Preferences ONLY ONCE at the start of the scan
        DataStore store = new DataStore();
        boolean skipHidden = store.isSkipHidden();
        long minSizeInBytes = store.getMinFileSizeKB() * 1024; // Convert KB to Bytes

        // 2. Start the fast recursive scan
        scanRecursive(root, fileList, skipHidden, minSizeInBytes);
        
        System.out.println("--- SCAN SUMMARY ---");
        System.out.println("Files successfully read: " + fileList.size());
        System.out.println("Files skipped (due to settings/permissions): " + skippedCount);
        
        return fileList;
    }

    private void scanRecursive(File folder, List<FileData> fileList, boolean skipHidden, long minSizeInBytes) {
        File[] files = folder.listFiles();
        
        // If files is null, it means we don't have permission to read this folder
        if (files == null) {
            skippedCount++;
            return;
        }

        for (File file : files) {
            // Rule 1: Skip hidden files
            if (skipHidden && file.isHidden()) {
                skippedCount++;
                continue; 
            }

            if (file.isDirectory()) {
                // If it's a folder, dive into it
                scanRecursive(file, fileList, skipHidden, minSizeInBytes);
            } else {
                long size = file.length();
                
                // Rule 2: Skip files smaller than minimum limit
                if (size < minSizeInBytes) {
                    skippedCount++;
                    continue;
                }

                String name = file.getName();
                String fullPath = file.getAbsolutePath();
                String type = getFileExtension(name);

                fileList.add(new FileData(name, type, size, fullPath));
            }
        }
    }
 // UPGRADED: Crash-proof recursive scanner
    private void scanRecursive1(File folder, List<FileData> fileList, boolean skipHidden, long minSizeInBytes) {
        try {
            // NEW DEFENSE 1: Prevent infinite loops from Linux Symlinks
            if (java.nio.file.Files.isSymbolicLink(folder.toPath())) {
                skippedCount++;
                return;
            }

            File[] files = folder.listFiles();
            
            // NEW DEFENSE 2: If files is null, the OS blocked us (Permission Denied)
            if (files == null) {
                skippedCount++;
                return;
            }

            for (File file : files) {
                try {
                    // Rule 1: Skip hidden files
                    if (skipHidden && file.isHidden()) {
                        skippedCount++;
                        continue; 
                    }

                    if (file.isDirectory()) {
                        // Dive into the next folder
                        scanRecursive(file, fileList, skipHidden, minSizeInBytes);
                    } else {
                        long size = file.length();
                        
                        // Rule 2: Skip files smaller than minimum limit
                        if (size < minSizeInBytes) {
                            skippedCount++;
                            continue;
                        }

                        String name = file.getName();
                        String fullPath = file.getAbsolutePath();
                        String type = getFileExtension(name);

                        fileList.add(new FileData(name, type, size, fullPath));
                    }
                // NEW DEFENSE 3: Catch any file-specific security lockouts
                } catch (SecurityException se) {
                    skippedCount++;
                }
            }
        // NEW DEFENSE 4: Catch any catastrophic folder read errors so the survives
        } catch (Exception e) {
            System.out.println("Skipped unreadable system folder: " + folder.getAbsolutePath());
            skippedCount++;
        }
    }
    public int getSkippedCount() {
        return skippedCount;
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "unknown";
        }
        return fileName.substring(lastDotIndex + 1);
    }
} 
