
package com.example.pr_1_file_dupe.service.CLI;

import java.io.File;

import com.example.pr_1_file_dupe.service.FileScanner; // adjust if your package name differs

public class CommandHandler {

    public static void handle(String[] args) {

        // 1. Help command
        if (args.length > 0 && args[0].equalsIgnoreCase("--help")) {
//            printHelp();
            return;
        }

        // 2. Decide path
        String path;

        if (args.length == 0) {
            // current directory
            path = System.getProperty("user.dir");
        } else {
            path = args[0];
        }

        System.out.println("Scanning directory: " + path);

        // 3. Validate path
        File dir = new File(path);

    }
    }
