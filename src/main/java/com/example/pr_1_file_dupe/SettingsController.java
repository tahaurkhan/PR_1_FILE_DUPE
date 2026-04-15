package com.example.pr_1_file_dupe;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;

public class SettingsController {

    @FXML private ComboBox<String> algoDropdown;
    @FXML private CheckBox skipHiddenCheckbox;
    @FXML private TextField minSizeInput;
    @FXML private Label statusLabel;
    @FXML private ToggleButton darkThemeToggle;
    @FXML private ToggleButton safeModeToggle;
    @FXML private Label safeModeDesc;

    private DataStore store;

    @FXML
    public void initialize() {

        store = new DataStore();

        // =============================
        // LOAD SAVED SETTINGS
        // =============================
        algoDropdown.setValue(store.getHashAlgorithm());
        skipHiddenCheckbox.setSelected(store.isSkipHidden());
        minSizeInput.setText(String.valueOf(store.getMinFileSizeKB()));

        boolean dark = store.isDarkTheme();
        darkThemeToggle.setSelected(dark);
        updateDarkButtonUI(dark);

        boolean safe = store.isSafeMode();
        safeModeToggle.setSelected(safe);
        updateSafeModeUI(safe);

        // =============================
        // 🔥 AUTO APPLY SETTINGS
        // =============================

        // 🌙 DARK MODE (INSTANT)
        darkThemeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            store.setDarkTheme(newVal);
            updateDarkButtonUI(newVal);
            applyTheme(newVal);
            statusLabel.setText(newVal ? "Dark mode enabled" : "Light mode enabled");
        });

        // 🔒 SAFE MODE (INSTANT)
        safeModeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            store.setSafeMode(newVal);
            updateSafeModeUI(newVal);

            if (!newVal) {
                showWarning();
            }

            statusLabel.setText(newVal
                    ? "Safe mode ON"
                    : "⚠ Safe mode OFF");
        });

        // ⚙️ ALGORITHM (INSTANT)
        algoDropdown.valueProperty().addListener((obs, oldVal, newVal) -> {
            store.setHashAlgorithm(newVal);
        });

        // 📁 SKIP HIDDEN (INSTANT)
        skipHiddenCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            store.setSkipHidden(newVal);
        });

        // 📏 MIN SIZE (INSTANT)
        minSizeInput.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                long value = Long.parseLong(newVal);
                store.setMinFileSizeKB(value);
                statusLabel.setText("Updated");
            } catch (Exception e) {
                statusLabel.setText("Enter valid number");
            }
        });
    }

    // =============================
    // 🎨 DARK MODE UI
    // =============================
    private void updateDarkButtonUI(boolean dark) {
        darkThemeToggle.setText(dark ? "ON" : "OFF");
        darkThemeToggle.setStyle(dark
                ? "-fx-background-color: #006565; -fx-text-fill: white; -fx-background-radius: 20;"
                : "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-background-radius: 20;");
    }

    private void applyTheme(boolean dark) {
        Scene scene = darkThemeToggle.getScene();
        scene.getStylesheets().clear();

        String css = dark ? "/styles/dark.css" : "/styles/light.css";
        scene.getStylesheets().add(getClass().getResource(css).toExternalForm());
    }

    // =============================
    // 🔒 SAFE MODE UI
    // =============================
    private void updateSafeModeUI(boolean safe) {
        safeModeToggle.setText(safe ? "ON" : "OFF");
        safeModeToggle.setStyle(safe
                ? "-fx-background-color: #27ae60; -fx-text-fill: white;"
                : "-fx-background-color: #e74c3c; -fx-text-fill: white;");

        safeModeDesc.setText(safe
                ? "ON — Files moved to Trash"
                : "OFF — Permanent delete ⚠");
    }

    private void showWarning() {
        Alert warn = new Alert(Alert.AlertType.WARNING);
        warn.setTitle("Warning");
        warn.setHeaderText("Permanent Delete Enabled");
        warn.setContentText("Files will be permanently deleted.");
        warn.showAndWait();
    }
}