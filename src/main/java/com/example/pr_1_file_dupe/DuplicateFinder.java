package com.example.pr_1_file_dupe;
//
//public class dupelicateFinder {
//
//	public dupelicateFinder() {
//		// TODO Auto-generated constructor stub
//	}
//
//}

import com.example.pr_1_file_dupe.FileData;
import com.example.pr_1_file_dupe.HashUtil;

import java.io.File;
import java.util.*;

public class DuplicateFinder {

    public Map<String, List<FileData>> findDuplicates(List<FileData> files) {

        Map<String, List<FileData>> hashMap = new HashMap<>();

        for (FileData fileData : files) {

            File file = new File(fileData.getPath());

            String hash = HashUtil.generateHash(file);

            if (hash == null) continue;

            hashMap.putIfAbsent(hash, new ArrayList<>());
            hashMap.get(hash).add(fileData);
        }

        return hashMap;
    }

    // Optional: get only duplicates
    public Map<String, List<FileData>> getOnlyDuplicates(Map<String, List<FileData>> map) {

        Map<String, List<FileData>> duplicates = new HashMap<>();

        for (Map.Entry<String, List<FileData>> entry : map.entrySet()) {
            if (entry.getValue().size() > 1) {
                duplicates.put(entry.getKey(), entry.getValue());
            }
        }

        return duplicates;
    }
}