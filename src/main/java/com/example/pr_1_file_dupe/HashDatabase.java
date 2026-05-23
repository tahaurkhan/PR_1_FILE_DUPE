package com.example.pr_1_file_dupe;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Persistent hash cache to speed up repeat scans.
 * Stores: filepath -> hash + lastModified
 */
public class HashDatabase {

    private static final String CACHE_FILE = "hash_cache.db";
    private Map<String, CachedHash> cache = new HashMap<>();

    public HashDatabase() {
        load();
    }

    // 🔹 Get cached hash if file hasn't changed
    public String getCachedHash(String filePath, long currentModified) {
        CachedHash entry = cache.get(filePath);
        
        if (entry != null && entry.lastModified == currentModified) {
            return entry.hash; // File unchanged, use cached hash
        }
        
        return null; // File changed or not in cache
    }

    // 🔹 Store hash for a file
    public void putHash(String filePath, String hash, long lastModified) {
        cache.put(filePath, new CachedHash(hash, lastModified));
    }

    // 🔹 Save cache to disk
    public void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CACHE_FILE))) {
            oos.writeObject(cache);
            System.out.println("✅ Hash cache saved: " + cache.size() + " entries");
        } catch (IOException e) {
            System.err.println("Failed to save hash cache: " + e.getMessage());
        }
    }

    // 🔹 Load cache from disk
    @SuppressWarnings("unchecked")
    private void load() {
        File file = new File(CACHE_FILE);
        if (!file.exists()) {
            System.out.println("No existing hash cache found. Starting fresh.");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            cache = (Map<String, CachedHash>) ois.readObject();
            System.out.println("✅ Hash cache loaded: " + cache.size() + " entries");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load hash cache: " + e.getMessage());
            cache = new HashMap<>(); // Start fresh if corrupted
        }
    }

    // 🔥 FIXED: Static method that correctly deletes the Java Serialization file
    public static void clearDatabase() {
        File cacheFile = new File(CACHE_FILE);
        if (cacheFile.exists()) {
            if (cacheFile.delete()) {
                System.out.println("✅ Database cache file deleted successfully.");
            } else {
                System.out.println("❌ Failed to delete cache file.");
            }
        } else {
            System.out.println("✅ Cache file is already empty.");
        }
    }

    // 🔹 Clear entire cache for the current instance
    public void clearCache() {
        cache.clear();
        clearDatabase(); // Re-use our static method to delete the file
        System.out.println("✅ In-memory hash cache cleared");
    }

    // 🔹 Check if all files in the scanned folder exist in cache with matching timestamps, and no files were added or deleted
    public boolean isFolderIdenticalToDatabase(java.util.List<FileData> scannedFiles, String folderPath) {
        if (scannedFiles == null) {
            return false;
        }

        File dir = new File(folderPath);
        if (!dir.exists()) {
            return false;
        }
        String normalizedFolder = dir.getAbsolutePath();

        // Count how many files under this folder are stored in the database cache
        int dbCountForFolder = 0;
        for (String filePath : cache.keySet()) {
            File f = new File(filePath);
            if (f.getAbsolutePath().startsWith(normalizedFolder)) {
                dbCountForFolder++;
            }
        }

        // If counts differ, there's a difference (files added or deleted)
        if (dbCountForFolder != scannedFiles.size()) {
            return false;
        }

        // Check if every scanned file is in cache and matches the modification time exactly
        for (FileData file : scannedFiles) {
            String path = file.getPath();
            CachedHash entry = cache.get(path);
            if (entry == null) {
                return false;
            }
            File f = new File(path);
            if (entry.lastModified != f.lastModified()) {
                return false;
            }
        }

        return true;
    }

    // 🔹 Remove all cache entries under a folder path to scan as new
    public void clearFolderCache(String folderPath) {
        File dir = new File(folderPath);
        if (!dir.exists()) return;
        String normalizedFolder = dir.getAbsolutePath();

        int initialSize = cache.size();
        cache.entrySet().removeIf(entry -> {
            File f = new File(entry.getKey());
            return f.getAbsolutePath().startsWith(normalizedFolder);
        });

        if (cache.size() != initialSize) {
            save();
            System.out.println("🧹 Removed " + (initialSize - cache.size()) + " cache entries under folder: " + folderPath);
        }
    }

    // 🔹 Remove stale entries (files that no longer exist)
    public void cleanStaleEntries() {
        cache.entrySet().removeIf(entry -> !new File(entry.getKey()).exists());
        save();
        System.out.println("✅ Cleaned stale cache entries");
    }

    // 📦 Inner class to store hash + timestamp
    private static class CachedHash implements Serializable {
        private static final long serialVersionUID = 1L;
        
        String hash;
        long lastModified;

        CachedHash(String hash, long lastModified) {
            this.hash = hash;
            this.lastModified = lastModified;
        }
    }
}