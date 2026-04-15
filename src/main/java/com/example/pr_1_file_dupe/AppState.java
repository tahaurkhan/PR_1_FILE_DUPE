package com.example.pr_1_file_dupe;

import java.util.ArrayList;
import java.util.List;

public class AppState {

    private static final List<String> pathHistory = new ArrayList<>();

    public static void addPath(String path) {
        if (path == null || path.isEmpty()) return;

        // remove duplicate if already exists
        pathHistory.remove(path);

        // add at top
        pathHistory.add(0, path);

        // limit history (like Google recent searches)
        if (pathHistory.size() > 10) {
            pathHistory.remove(pathHistory.size() - 1);
        }
    }

    public static List<String> getPathHistory() {
        return pathHistory;
    }
}