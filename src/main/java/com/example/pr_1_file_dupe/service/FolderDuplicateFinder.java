package com.example.pr_1_file_dupe.service;

import com.example.pr_1_file_dupe.FileData;
import com.example.pr_1_file_dupe.HashUtil;

import java.io.File;
import java.util.*;

public class FolderDuplicateFinder {

    // 🔹 MAIN METHOD
    public List<List<String>> findDuplicateFolders(String rootPath) {

        Map<String, List<String>> folderMap = new HashMap<>();

        File root = new File(rootPath);

        // Step 1: get all folders
        List<File> folders = getAllFolders(root);

        // Step 2: generate hash for each folder
        for (File folder : folders) {

            String folderHash = generateFolderHash(folder);

            folderMap.putIfAbsent(folderHash, new ArrayList<>());
            folderMap.get(folderHash).add(folder.getAbsolutePath());
        }

        // Step 3: filter duplicate folders
        Map<String, List<String>> duplicateFolders = getOnlyDuplicateFolders(folderMap);

        // Step 4: remove nested duplicates
        return filterRootDuplicates(duplicateFolders);
    }

    // 🔹 GET ALL FOLDERS (RECURSIVE)
    private List<File> getAllFolders(File root) {

        List<File> folders = new ArrayList<>();

        if (root.isDirectory()) {
            folders.add(root);

            File[] files = root.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        folders.addAll(getAllFolders(f));
                    }
                }
            }
        }

        return folders;
    }

    // 🔹 GENERATE FOLDER HASH
    private String generateFolderHash(File folder) {

        List<String> fileHashes = new ArrayList<>();

        FileScanner scanner = new FileScanner();
        List<FileData> files = scanner.scanDirectory(folder.getAbsolutePath());

        for (FileData fileData : files) {
            File f = new File(fileData.getPath());
            String hash = HashUtil.generateHash(f);

            if (hash != null) {
                fileHashes.add(hash);
            }
        }

        // sort hashes
        Collections.sort(fileHashes);

        // combine hashes
        StringBuilder combined = new StringBuilder();
        for (String h : fileHashes) {
            combined.append(h);
        }

        return combined.toString();
    }

    // 🔹 FILTER ONLY DUPLICATES
    private Map<String, List<String>> getOnlyDuplicateFolders(Map<String, List<String>> map) {

        Map<String, List<String>> duplicates = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            if (entry.getValue().size() > 1) {
                duplicates.put(entry.getKey(), entry.getValue());
            }
        }

        return duplicates;
    }

    // 🔥 REMOVE NESTED DUPLICATES
    private List<List<String>> filterRootDuplicates(Map<String, List<String>> duplicateFolders) {

        List<List<String>> finalGroups = new ArrayList<>();

        for (List<String> group : duplicateFolders.values()) {

            List<String> filtered = new ArrayList<>();

            for (String folder : group) {

                if (!isParentDuplicate(folder, duplicateFolders)) {
                    filtered.add(folder);
                }
            }

            if (filtered.size() > 1) {
                finalGroups.add(filtered);
            }
        }

        return finalGroups;
    }

    // 🔹 CHECK IF PARENT IS ALSO DUPLICATE
    private boolean isParentDuplicate(String folder,
                                      Map<String, List<String>> duplicateFolders) {

        File parent = new File(folder).getParentFile();

        if (parent == null) return false;

        String parentPath = parent.getAbsolutePath();

        for (List<String> group : duplicateFolders.values()) {
            if (group.contains(parentPath)) {
                return true;
            }
        }

        return false;
    }
}