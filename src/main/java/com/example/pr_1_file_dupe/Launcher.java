package com.example.pr_1_file_dupe;

import com.example.pr_1_file_dupe.service.CLI.CommandHandler;

public class Launcher {
    public static void main(String[] args) {
        if (args.length > 0) {
            // CLI Mode: If there are arguments, use the CommandHandler
            CommandHandler cli = new CommandHandler();
            cli.handle(args);
        } else {
            // GUI Mode: If no arguments, launch JavaFX
            HelloApplication.main(args);
        }
    }
}