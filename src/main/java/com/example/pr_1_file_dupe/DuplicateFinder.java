package com.example.pr_1_file_dupe;

import com.example.pr_1_file_dupe.DataStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DuplicateFinder {

    public Map<String, List<FileData>> findDuplicates(List<FileData> allFiles) {
        System.out.println("Starting duplicate analysis...");

        // 1. Get the user's preferred algorithm from Settings
        DataStore store = new DataStore();
        String selectedAlgorithm = store.getHashAlgorithm();
        System.out.println("Using Algorithm: " + selectedAlgorithm);

        // Step 1: Group by Size (Fast)
        Map<Long, List<FileData>> sizeMap = new HashMap<>();
        for (FileData file : allFiles) {
            sizeMap.computeIfAbsent(file.getSize(), k -> new ArrayList<>()).add(file);
        }

        // Step 2: Group by Hash (Only for files that share the exact same size)
        Map<String, List<FileData>> hashMap = new HashMap<>();
        int hashedCount = 0;

        for (List<FileData> sameSizeFiles : sizeMap.values()) {
            if (sameSizeFiles.size() > 1)
            { 
                for (FileData file : sameSizeFiles) {
                    try {
                        // NEW: Pass the algorithm to HashUtil
                        String hash = HashUtil.getFileChecksum(file.getPath(), selectedAlgorithm);
                        hashMap.computeIfAbsent(hash, k -> new ArrayList<>()).add(file);
                        hashedCount++;
                    } catch (Exception e) {
                        System.out.println("Skipped unreadable file: " + file.getName());
                    }
                }
            }
        }

        // Step 3: Filter out unique files
        Map<String, List<FileData>> duplicatesOnly = new HashMap<>();
        for (Map.Entry<String, List<FileData>> entry : hashMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                duplicatesOnly.put(entry.getKey(), entry.getValue());
            }
        }

        System.out.println("Total files hashed: " + hashedCount);
        return duplicatesOnly;
    }
}