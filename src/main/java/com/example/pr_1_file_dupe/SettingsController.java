package com.example.pr_1_file_dupe;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import com.example.pr_1_file_dupe.HashDatabase;

public class SettingsController {

    @FXML
    private ComboBox<String> algoDropdown;
    @FXML
    private CheckBox skipHiddenCheckbox;
    @FXML
    private CheckBox skipSystemFilesCheckbox;
    @FXML
    private TextField minSizeInput;
    @FXML
    private Label statusLabel;
    @FXML
    private ToggleButton safeModeToggle;
    @FXML
    private Label safeModeDesc;
    @FXML
    private ToggleButton soundToggle;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ToggleButton themeToggle;
    @FXML
    private Label themeDesc;

    private DataStore store;
    private PauseTransition saveIndicator; // Used to hide the "Saved ✓" text after a few seconds

    @FXML
    public void initialize() {
        store = new DataStore();

        // Setup the fading "Saved" indicator timer
        saveIndicator = new PauseTransition(Duration.seconds(2));
        saveIndicator.setOnFinished(e -> statusLabel.setText(""));

        // 1. LOAD INITIAL VALUES FROM DATASTORE
        algoDropdown.setValue(store.getHashAlgorithm());
        skipHiddenCheckbox.setSelected(store.isSkipHidden());
        skipSystemFilesCheckbox.setSelected(store.isSkipSystemFiles());
        minSizeInput.setText(String.valueOf(store.getMinFileSizeKB()));

        // Load Sound
        soundToggle.setSelected(store.isSoundEnabled());
        soundToggle.setText(store.isSoundEnabled() ? "ON" : "OFF");
        volumeSlider.setValue(store.getSoundVolume() * 100.0);

        // Load Safe Mode
        boolean initialSafe = store.isSafeMode();
        safeModeToggle.setSelected(initialSafe);
        updateSafeModeUI(initialSafe);

        // Load Theme
        boolean initialDark = store.isDarkTheme();
        themeToggle.setSelected(initialDark);
        updateThemeUI(initialDark);

        // ---------------------------------------------------------
        // 2. ATTACH AUTO-SAVE LISTENERS
        // ---------------------------------------------------------
        algoDropdown.valueProperty().addListener((obs, oldVal, newVal) -> {
            store.setHashAlgorithm(newVal);
            showSaved();
        });

        skipHiddenCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            store.setSkipHidden(newVal);
            showSaved();
        });

        skipSystemFilesCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            store.setSkipSystemFiles(newVal);
            showSaved();
        });

        // Smart text listener: Forces numeric input only, saves instantly
        minSizeInput.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                minSizeInput.setText(newVal.replaceAll("[^\\d]", ""));
            } else if (!newVal.isEmpty()) {
                try {
                    store.setMinFileSizeKB(Long.parseLong(newVal));
                    showSaved();
                } catch (NumberFormatException ignored) {
                }
            }
        });

        soundToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            store.setSoundEnabled(newVal);
            soundToggle.setText(newVal ? "ON" : "OFF");
            com.example.pr_1_file_dupe.utils.SoundManager.setSoundEnabled(newVal);
            showSaved();
        });

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double vol = newVal.doubleValue() / 100.0;
            store.setSoundVolume(vol);
            com.example.pr_1_file_dupe.utils.SoundManager.setVolume(vol);
            showSaved();
        });

        safeModeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            store.setSafeMode(newVal);
            updateSafeModeUI(newVal);

            if (!newVal) {
                Alert warn = new Alert(Alert.AlertType.WARNING);
                warn.setTitle("Safe Mode Disabled");
                warn.setHeaderText("⚠ Permanent deletion enabled");
                warn.setContentText("Safe mode is OFF.\nFiles will be PERMANENTLY deleted.\nAre you sure?");
                warn.showAndWait();
            }
            showSaved();
        });

        themeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            store.setDarkTheme(newVal);
            updateThemeUI(newVal);
            applyThemeToScene(themeToggle.getScene(), newVal);
            showSaved();
        });
    }

    // Helper to update Safe Mode colors
    private void updateSafeModeUI(boolean safe) {
        safeModeToggle.setText(safe ? "ON" : "OFF");
        safeModeToggle.setStyle(safe
                ? "-fx-background-color: -success; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 5 18; -fx-font-weight: bold;"
                : "-fx-background-color: -danger; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 5 18; -fx-font-weight: bold;"
        );
        safeModeDesc.setText(safe ? "ON — Files moved to Trash (recoverable)" : "OFF — Files permanently deleted ⚠");
        safeModeDesc.setStyle(safe ? "-fx-text-fill: -success; -fx-font-size: 12px;" : "-fx-text-fill: -danger; -fx-font-size: 12px; -fx-font-weight: bold;");
    }

    // Helper to update Theme toggle labels/styling
    private void updateThemeUI(boolean dark) {
        themeToggle.setText(dark ? "ON" : "OFF");
        themeToggle.setStyle(dark
                ? "-fx-background-color: -success; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 5 18; -fx-font-weight: bold;"
                : "-fx-background-color: -on-muted; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 5 18; -fx-font-weight: bold;"
        );
        themeDesc.setText(dark ? "ON — Dark theme active 🌙" : "OFF — Light theme active ☀️");
        themeDesc.setStyle(dark ? "-fx-text-fill: -success; -fx-font-size: 12px;" : "-fx-text-fill: -on-muted; -fx-font-size: 12px;");
    }

    // Apply selected theme stylesheet to active Scene
    private void applyThemeToScene(javafx.scene.Scene scene, boolean dark) {
        if (scene != null) {
            scene.getStylesheets().clear();
            if (dark) {
                scene.getStylesheets().add(getClass().getResource("/com/example/pr_1_file_dupe/CSS/dark-theme.css").toExternalForm());
            } else {
                scene.getStylesheets().add(getClass().getResource("/com/example/pr_1_file_dupe/CSS/application.css").toExternalForm());
            }
        }
    }

    // Displays the "Auto-saved" text and fades it out after 2 seconds
    private void showSaved() {
        statusLabel.setStyle("-fx-text-fill: -success; -fx-font-style: italic;");
        statusLabel.setText("Settings auto-saved ✓");
        saveIndicator.playFromStart(); // Reset timer
    }

    // Button to manually clear the database cache
    @FXML
    public void clearHashCache() {
        new HashDatabase().clearDatabase();
        statusLabel.setStyle("-fx-text-fill: -warning; -fx-font-weight: bold;");
        statusLabel.setText("Database Cache Cleared!");
        saveIndicator.playFromStart();
    }
}
