package com.example.pr_1_file_dupe.service;

import com.example.pr_1_file_dupe.DataStore;
import com.example.pr_1_file_dupe.FileData;
import com.example.pr_1_file_dupe.utils.SystemFileFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced FileScanner with System File Protection
 * - Skips Windows/Linux system directories
 * - Detects and skips locked files
 * - Faster and safer for system-wide scans
 */
public class EnhancedFileScanner {

    private int skippedCount = 0;
    private int systemFilesSkipped = 0;
    private int lockedFilesSkipped = 0;

    public List<FileData> scanDirectory(String path) {
        List<FileData> fileList = new ArrayList<>();
        File root = new File(path);

        if (!root.exists()) {
            System.out.println("Path does not exist!");
            return fileList;
        }

        // Load User Preferences
        DataStore store = new DataStore();
        boolean skipHidden = store.isSkipHidden();
        boolean skipSystemFiles = store.isSkipSystemFiles(); // NEW SETTING
        long minSizeInBytes = store.getMinFileSizeKB() * 1024;

        // Reset counters
        skippedCount = 0;
        systemFilesSkipped = 0;
        lockedFilesSkipped = 0;

        // Start scan
        System.out.println("🔍 Starting enhanced scan with system protection...");
        scanRecursive(root, fileList, skipHidden, skipSystemFiles, minSizeInBytes);
        
        System.out.println("\n═══ SCAN SUMMARY ═══");
        System.out.println("✓ Files successfully read: " + fileList.size());
        System.out.println("⊘ System files skipped: " + systemFilesSkipped);
        System.out.println("🔒 Locked files skipped: " + lockedFilesSkipped);
        System.out.println("⚙ Other files skipped: " + (skippedCount - systemFilesSkipped - lockedFilesSkipped));
        System.out.println("═══════════════════════\n");
        
        return fileList;
    }

    /**
     * Recursive scanner with enhanced protection
     */
    private void scanRecursive(File folder, List<FileData> fileList, 
                               boolean skipHidden, boolean skipSystemFiles, 
                               long minSizeInBytes) {
        try {
            // DEFENSE 1: Skip system directories immediately
            if (skipSystemFiles && SystemFileFilter.isSystemFile(folder)) {
                systemFilesSkipped++;
                return;
            }

            // DEFENSE 2: Prevent infinite loops from symlinks
            if (java.nio.file.Files.isSymbolicLink(folder.toPath())) {
                skippedCount++;
                return;
            }

            File[] files = folder.listFiles();
            
            // DEFENSE 3: Permission denied protection
            if (files == null) {
                skippedCount++;
                return;
            }

            for (File file : files) {
                try {
                    // Skip hidden files if enabled
                    if (skipHidden && file.isHidden()) {
                        skippedCount++;
                        continue; 
                    }

                    // Skip system files if enabled (NEW)
                    if (skipSystemFiles && SystemFileFilter.isSystemFile(file)) {
                        systemFilesSkipped++;
                        continue;
                    }

                    if (file.isDirectory()) {
                        // Recursively scan subdirectories
                        scanRecursive(file, fileList, skipHidden, skipSystemFiles, minSizeInBytes);
                    } else {
                        // Skip locked files (NEW)
                        if (SystemFileFilter.isFileLocked(file)) {
                            lockedFilesSkipped++;
                            continue;
                        }

                        long size = file.length();
                        
                        // Skip files smaller than minimum limit
                        if (size < minSizeInBytes) {
                            skippedCount++;
                            continue;
                        }

                        String name = file.getName();
                        String fullPath = file.getAbsolutePath();
                        String type = getFileExtension(name);

                        fileList.add(new FileData(name, type, size, fullPath));
                    }

                } catch (SecurityException se) {
                    // File-specific security lockout
                    skippedCount++;
                }
            }

        } catch (Exception e) {
            // Folder-level error protection
            System.out.println("⚠ Skipped protected folder: " + folder.getAbsolutePath());
            skippedCount++;
        }
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public int getSystemFilesSkipped() {
        return systemFilesSkipped;
    }

    public int getLockedFilesSkipped() {
        return lockedFilesSkipped;
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "unknown";
        }
        return fileName.substring(lastDotIndex + 1);
    }
}