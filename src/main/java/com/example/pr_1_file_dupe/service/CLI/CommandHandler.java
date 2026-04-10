package com.example.pr_1_file_dupe.service.CLI;

import com.example.pr_1_file_dupe.service.FileScanner;
import com.example.pr_1_file_dupe.DuplicateFinder;
import com.example.pr_1_file_dupe.FileData;
import java.util.List;
import java.util.Map;

public class CommandHandler {

    public void handle(String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("--help")) {
            printHelp();
            return;
        }

        String path = args[0];
        System.out.println("CLI Mode: Scanning path -> " + path);

        // 1. Reuse your existing Scanner logic
        FileScanner scanner = new FileScanner();
        List<FileData> allFiles = scanner.scanDirectory(path);

        // 2. Reuse your existing Duplicate Finder
        DuplicateFinder finder = new DuplicateFinder();
        Map<String, List<FileData>> duplicates = finder.findDuplicates(allFiles);

        // 3. Output results to the terminal
        System.out.println("\n--- CLI SCAN RESULTS ---");
        System.out.println("Total Files Scanned: " + allFiles.size());
        System.out.println("Duplicate Groups Found: " + duplicates.size());
        System.out.println("------------------------");

        for (Map.Entry<String, List<FileData>> entry : duplicates.entrySet()) {
            System.out.println("\nGroup: " + entry.getKey());
            for (FileData file : entry.getValue()) {
                System.out.println("  [Duplicate] -> " + file.getPath());
            }
        }
    }

    private void printHelp() {
        System.out.println("Usage: java -jar FileDupe.jar [PATH]");
        System.out.println("Example: java -jar FileDupe.jar /home/tahaur/Downloads");
    }
}