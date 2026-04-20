package com.example.pr_1_file_dupe;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(CACHE_FILE))) {
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

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file))) {
            cache = (Map<String, CachedHash>) ois.readObject();
            System.out.println("✅ Hash cache loaded: " + cache.size() + " entries");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load hash cache: " + e.getMessage());
            cache = new HashMap<>(); // Start fresh if corrupted
        }
    }

    // 🔹 Clear entire cache
    public void clearCache() {
        cache.clear();
        File cacheFile = new File(CACHE_FILE);
        if (cacheFile.exists()) {
            cacheFile.delete();
        }
        System.out.println("✅ Hash cache cleared");
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