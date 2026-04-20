package com.example.pr_1_file_dupe;

import java.io.File;
import java.util.*;

public class DuplicateFinder {

    public Map<String, List<FileData>> findDuplicates(List<FileData> allFiles) {
        System.out.println("Starting duplicate analysis...");

        // 1. Load hash cache
        HashDatabase hashDB = new HashDatabase();

        DataStore store = new DataStore();
        String selectedAlgorithm = store.getHashAlgorithm();
        System.out.println("Using Algorithm: " + selectedAlgorithm);

        // Step 1: Group by Size
        Map<Long, List<FileData>> sizeMap = new HashMap<>();
        for (FileData file : allFiles) {
            sizeMap.computeIfAbsent(file.getSize(), k -> new ArrayList<>()).add(file);
        }

        // Step 2: Group by Hash
        Map<String, List<FileData>> hashMap = new HashMap<>();
        int hashedCount = 0;
        int cachedCount = 0;

        for (List<FileData> sameSizeFiles : sizeMap.values()) {
            if (sameSizeFiles.size() > 1) {
                for (FileData file : sameSizeFiles) {
                    try {
                        File f = new File(file.getPath());
                        long lastModified = f.lastModified();

                        String hash = hashDB.getCachedHash(file.getPath(), lastModified);

                        if (hash == null) {
                            hash = HashUtil.getFileChecksum(file.getPath(), selectedAlgorithm);
                            hashDB.putHash(file.getPath(), hash, lastModified);
                            hashedCount++;
                        } else {
                            cachedCount++;
                        }

                        hashMap.computeIfAbsent(hash, k -> new ArrayList<>()).add(file);

                    } catch (Exception e) {
                        System.out.println("Skipped unreadable file: " + file.getName());
                    }
                }
            }
        }

        hashDB.save();

        // Step 3: Filter duplicates
        return getOnlyDuplicates(hashMap);
    }

    public Map<String, List<FileData>> getOnlyDuplicates(
            Map<String, List<FileData>> allResults) {

        Map<String, List<FileData>> duplicatesOnly = new HashMap<>();

        for (Map.Entry<String, List<FileData>> entry : allResults.entrySet()) {
            if (entry.getValue().size() > 1) {
                duplicatesOnly.put(entry.getKey(), entry.getValue());
            }
        }

        return duplicatesOnly;
    }
}