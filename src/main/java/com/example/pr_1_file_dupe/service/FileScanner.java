package com.example.pr_1_file_dupe.service;

import com.example.pr_1_file_dupe.DataStore;
import com.example.pr_1_file_dupe.FileData;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FileScanner {

    private int skippedCount = 0;
    private int throttleCounter = 0; // Throttles UI updates to prevent freezing

    /**
     * 🔥 FULL SYSTEM SCAN - Scans all user-accessible drives
     */
    public List<FileData> scanFullSystem(Consumer<String> progressCallback) {
        List<FileData> allFiles = new ArrayList<>();
        File[] roots = File.listRoots();
        
        if (progressCallback != null) {
            progressCallback.accept("0:::Detecting system drives...");
        }

        System.out.println("🔍 Starting FULL SYSTEM SCAN...");
        System.out.println("📂 Detected " + roots.length + " drive(s)");
        
        for (File root : roots) {
            // 🔥 LISTEN FOR CANCEL: Stop moving to the next drive if cancelled
            if (Thread.currentThread().isInterrupted()) break;
            
            System.out.println("Scanning: " + root.getAbsolutePath());
            allFiles.addAll(scanDirectory(root.getAbsolutePath(), progressCallback));
        }
        
        System.out.println("✅ System scan complete: " + allFiles.size() + " files found");
        return allFiles;
    }

    public List<FileData> scanDirectory(String path, Consumer<String> progressCallback) {
        List<FileData> fileList = new ArrayList<>();
        File root = new File(path);

        if (!root.exists()) {
            System.out.println("Path does not exist!");
            return fileList;
        }

        // Load settings once
        DataStore store = new DataStore();
        boolean skipHidden = store.isSkipHidden();
        long minSizeInBytes = store.getMinFileSizeKB() * 1024;

        // Start recursive scan
        scanRecursive(root, fileList, skipHidden, minSizeInBytes, progressCallback);
        
        System.out.println("--- SCAN SUMMARY ---");
        System.out.println("Files successfully read: " + fileList.size());
        System.out.println("Files skipped (settings/permissions): " + skippedCount);
        
        return fileList;
    }

    private void scanRecursive(File folder, List<FileData> fileList, 
                               boolean skipHidden, long minSizeInBytes, Consumer<String> progressCallback) {
        try {
            // 🔥 LISTEN FOR CANCEL: Stop recursion immediately if the user clicked cancel
            if (Thread.currentThread().isInterrupted()) return;

            // 🛡️ DEFENSE 1: Skip symbolic links
            if (java.nio.file.Files.isSymbolicLink(folder.toPath())) {
                skippedCount++;
                return;
            }
            
            // 🛡️ DEFENSE 2: Skip system-protected directories
            String folderName = folder.getName().toLowerCase();
            if (folderName.equals("windows") || 
                folderName.equals("system32") ||
                folderName.equals("$recycle.bin") ||
                folderName.equals("system volume information") ||
                folderName.equals("programdata") ||
                folderName.equals("program files") ||
                folderName.equals("program files (x86)") ||
                folderName.equals(".android") ||
                folderName.equals("appdata") ||
                folderName.equals("boot") ||
                folderName.equals("dev") ||
                folderName.equals("proc") ||
                folderName.equals("sys") ||
                folderName.equals("run") ||
                folderName.equals("snap")) {
                skippedCount++;
                return;
            }

            File[] files = folder.listFiles();
            
            // 🛡️ DEFENSE 3: Permission denied or locked folder
            if (files == null) {
                skippedCount++;
                return;
            }

            for (File file : files) {
                // 🔥 LISTEN FOR CANCEL: Stop checking files if cancelled
                if (Thread.currentThread().isInterrupted()) return;

                try {
                    // Skip hidden files if enabled
                    if (skipHidden && file.isHidden()) {
                        skippedCount++;
                        continue;
                    }

                    if (file.isDirectory()) {
                        scanRecursive(file, fileList, skipHidden, minSizeInBytes, progressCallback);
                    } else {
                        // 🛡️ DEFENSE 4: Skip locked/unreadable files
                        if (!file.canRead()) {
                            skippedCount++;
                            continue;
                        }
                        
                        long size = file.length();
                        
                        // Skip files below minimum size
                        if (size < minSizeInBytes) {
                            skippedCount++;
                            continue;
                        }

                        String name = file.getName();
                        String fullPath = file.getAbsolutePath();
                        String type = getFileExtension(name);

                        fileList.add(new FileData(name, type, size, fullPath));

                        // 🔥 WINDOWS THROTTLE: Send both count AND path separated by ":::"
                        throttleCounter++;
                        if (throttleCounter % 40 == 0 && progressCallback != null) {
                            progressCallback.accept(throttleCounter + ":::" + fullPath);
                        }
                    }
                    
                } catch (SecurityException se) {
                    skippedCount++;
                }
            }
            
        } catch (Exception e) {
            System.out.println("⚠ Skipped protected folder: " + folder.getAbsolutePath());
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