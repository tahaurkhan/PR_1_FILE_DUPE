package com.example.pr_1_file_dupe;

import javafx.scene.Scene;

public class ThemeManager {

    private static final String LIGHT_CSS = "/com/example/pr_1_file_dupe/CSS/application.css";
    private static final String DARK_CSS = "/com/example/pr_1_file_dupe/CSS/dark-theme.css";

    // Applies the correct theme to a scene instantly
    public static void apply(Scene scene) {

        if (scene == null) return;
        
        scene.getStylesheets().clear();
        
        // 🔥 FIX: ALWAYS load the base application styles so the layout doesn't break
        scene.getStylesheets().add(ThemeManager.class.getResource(LIGHT_CSS).toExternalForm());
        
        // 🔥 FIX: If Dark Mode is true, add the dark colors ON TOP of the base styles
        if (new DataStore().isDarkTheme()) {
            scene.getStylesheets().add(ThemeManager.class.getResource(DARK_CSS).toExternalForm());
        }
    
    }

    public static void applyToAllScenes(javafx.stage.Stage stage) {
        apply(stage.getScene());
    }
}