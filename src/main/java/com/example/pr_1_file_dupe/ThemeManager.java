package com.example.pr_1_file_dupe;

import javafx.scene.Scene;

public class ThemeManager {

    private static final String LIGHT_CSS =
            "/com/example/pr_1_file_dupe/CSS/application.css";
    private static final String DARK_CSS =
            "/com/example/pr_1_file_dupe/CSS/dark-theme.css";

    // Applies the correct theme to a scene instantly
    public static void apply(Scene scene) {
        scene.getStylesheets().clear();
        boolean dark = new DataStore().isDarkTheme();
        String css = ThemeManager.class.getResource(
                dark ? DARK_CSS : LIGHT_CSS).toExternalForm();
        scene.getStylesheets().add(css);
    }

    // Call this from any screen to re-apply after toggle
    public static void applyToAllScenes(javafx.stage.Stage stage) {
        apply(stage.getScene());
    }
}