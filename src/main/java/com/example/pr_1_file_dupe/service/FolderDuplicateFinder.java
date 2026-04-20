package com.example.pr_1_file_dupe.service;

import com.example.pr_1_file_dupe.FileData;
import com.example.pr_1_file_dupe.HashUtil;
import com.example.pr_1_file_dupe.utils.SystemFileFilter;
import java.io.File;
import java.util.*;

/**
 * FolderDuplicateDetector - Finds duplicate folders/programs
 * Shows only parent folders, not individual child files
 * Optimized for program and folder-level duplicate detection
 */
public class FolderDuplicateFinder {

    /**
     * Find duplicate folders (parent-level only)
     * @param rootPath Root directory to scan
     * @return List of duplicate folder groups
     */
    public List<DuplicateFolderGroup> findDuplicateFolders(String rootPath) {
        Map<String, List<String>> folderHashMap = new HashMap<>();
        File root = new File(rootPath);

        System.out.println("🗂 Scanning for duplicate folders...");

        // Step 1: Get all folders
        List<File> allFolders = getAllFolders(root);
        System.out.println("Found " + allFolders.size() + " folders to analyze");

        // Step 2: Generate hash for each folder
        int processed = 0;
        for (File folder : allFolders) {
            processed++;
            if (processed % 50 == 0) {
                System.out.println("Processing folder " + processed + "/" + allFolders.size());
            }

            String folderHash = generateFolderHash(folder);
            if (folderHash != null && !folderHash.isEmpty()) {
                folderHashMap.computeIfAbsent(folderHash, k -> new ArrayList<>())
                             .add(folder.getAbsolutePath());
            }
        }

        // Step 3: Filter to only duplicates
        List<DuplicateFolderGroup> duplicates = new ArrayList<>();
        
        for (Map.Entry<String, List<String>> entry : folderHashMap.entrySet()) {
            List<String> paths = entry.getValue();
            
            if (paths.size() > 1) {
                // Remove nested duplicates (only keep parent folders)
                List<String> parentOnly = filterToParentFoldersOnly(paths);
                
                if (parentOnly.size() > 1) {
                    long folderSize = calculateFolderSize(new File(parentOnly.get(0)));
                    duplicates.add(new DuplicateFolderGroup(parentOnly, folderSize));
                }
            }
        }

        System.out.println("✓ Found " + duplicates.size() + " duplicate folder groups");
        return duplicates;
    }

    /**
     * Get all folders recursively, skipping system folders
     */
    private List<File> getAllFolders(File root) {
        List<File> folders = new ArrayList<>();

        if (!root.isDirectory()) return folders;

        // Skip system folders
        if (SystemFileFilter.isSystemFile(root)) {
            return folders;
        }

        folders.add(root);

        File[] files = root.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory() && !SystemFileFilter.isSystemFile(f)) {
                    folders.addAll(getAllFolders(f));
                }
            }
        }

        return folders;
    }

    /**
     * Generate hash for entire folder contents
     * Uses combined hash of all files inside
     */
    private String generateFolderHash(File folder) {
        List<String> fileHashes = new ArrayList<>();

        try {
            File[] files = folder.listFiles();
            if (files == null || files.length == 0) {
                return null; // Empty folder
            }

            // Hash all files in this folder (not recursive)
            for (File file : files) {
                if (file.isFile() && !SystemFileFilter.isFileLocked(file)) {
                    String hash = HashUtil.generateHash(file);
                    if (hash != null) {
                        fileHashes.add(file.getName() + ":" + hash);
                    }
                }
            }

            if (fileHashes.isEmpty()) {
                return null;
            }

            // Sort to ensure consistent hash
            Collections.sort(fileHashes);

            // Combine all hashes
            StringBuilder combined = new StringBuilder();
            for (String h : fileHashes) {
                combined.append(h);
            }

            // Generate final hash from combined string
            return HashUtil.generateHash(
                new File(folder.getAbsolutePath() + "/.temp_hash_" + 
                         Integer.toHexString(combined.toString().hashCode()))
            );

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Remove nested duplicates - only keep top-level parent folders
     * If /A/B and /A/B/C are both duplicates, only keep /A/B
     */
    private List<String> filterToParentFoldersOnly(List<String> paths) {
        List<String> filtered = new ArrayList<>();

        for (String path : paths) {
            boolean isNested = false;

            // Check if this path is nested inside any other path
            for (String otherPath : paths) {
                if (!path.equals(otherPath) && path.startsWith(otherPath + File.separator)) {
                    isNested = true;
                    break;
                }
            }

            if (!isNested) {
                filtered.add(path);
            }
        }

        return filtered;
    }

    /**
     * Calculate total size of a folder
     */
    private long calculateFolderSize(File folder) {
        long size = 0;
        
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else if (file.isDirectory()) {
                    size += calculateFolderSize(file);
                }
            }
        }

        return size;
    }

    /**
     * Data class for duplicate folder groups
     */
    public static class DuplicateFolderGroup {
        private final List<String> paths;
        private final long size;

        public DuplicateFolderGroup(List<String> paths, long size) {
            this.paths = paths;
            this.size = size;
        }

        public List<String> getPaths() {
            return paths;
        }

        public long getSize() {
            return size;
        }

        public long getWastedSpace() {
            return size * (paths.size() - 1);
        }

        public int getCount() {
            return paths.size();
        }
    }
}