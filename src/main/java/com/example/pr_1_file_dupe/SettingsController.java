package com.example.pr_1_file_dupe;

import com.example.pr_1_file_dupe.utils.SoundManager;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;

/**
 *  Settings Controller
 * Adds controls for:
 * - System file filtering
 * - Sound effects toggle
 * - Volume control
 */
public class SettingsController {

    @FXML private ComboBox<String> algoDropdown;
    @FXML private CheckBox skipHiddenCheckbox;
    @FXML private CheckBox skipSystemFilesCheckbox; // NEW
    @FXML private TextField minSizeInput;
    @FXML private Label statusLabel;
    @FXML private ToggleButton darkThemeToggle;
    @FXML private ToggleButton safeModeToggle;
    @FXML private ToggleButton soundToggle; // NEW
    @FXML private Slider volumeSlider; // NEW
    @FXML private Label safeModeDesc;

    private DataStore store;

    @FXML
    public void initialize() {
        store = new DataStore();

        // ═══════════════════════════════════════════════
        // LOAD SAVED SETTINGS
        // ═══════════════════════════════════════════════
        algoDropdown.setValue(store.getHashAlgorithm());
        skipHiddenCheckbox.setSelected(store.isSkipHidden());
        skipSystemFilesCheckbox.setSelected(store.isSkipSystemFiles()); // NEW
        minSizeInput.setText(String.valueOf(store.getMinFileSizeKB()));

        boolean dark = store.isDarkTheme();
        darkThemeToggle.setSelected(dark);
        updateDarkButtonUI(dark);

        boolean safe = store.isSafeMode();
        safeModeToggle.setSelected(safe);
        updateSafeModeUI(safe);

        // NEW: Sound settings
        boolean soundEnabled = store.isSoundEnabled();
        soundToggle.setSelected(soundEnabled);
        updateSoundButtonUI(soundEnabled);
        SoundManager.setSoundEnabled(soundEnabled);

        if (volumeSlider != null) {
            volumeSlider.setValue(store.getSoundVolume() * 100);
            SoundManager.setVolume(store.getSoundVolume());
        }

        // ═══════════════════════════════════════════════
        // AUTO APPLY SETTINGS
        // ═══════════════════════════════════════════════

        // 🌙 DARK MODE
        darkThemeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            store.setDarkTheme(newVal);
            updateDarkButtonUI(newVal);
            applyTheme(newVal);
            statusLabel.setText(newVal ? "Dark mode enabled" : "Light mode enabled");
            SoundManager.play(SoundManager.Sound.BUTTON_CLICK);
        });

        // 🔒 SAFE MODE
        safeModeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            store.setSafeMode(newVal);
            updateSafeModeUI(newVal);

            if (!newVal) {
                showWarning();
            }

            statusLabel.setText(newVal ? "Safe mode ON" : "⚠ Safe mode OFF");
            SoundManager.play(SoundManager.Sound.BUTTON_CLICK);
        });

        // 🔊 SOUND TOGGLE (NEW)
        soundToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            store.setSoundEnabled(newVal);
            SoundManager.setSoundEnabled(newVal);
            updateSoundButtonUI(newVal);
            statusLabel.setText(newVal ? "Sound effects enabled" : "Sound effects disabled");
            
            if (newVal) {
                SoundManager.play(SoundManager.Sound.SUCCESS);
            }
        });

        // 🔊 VOLUME SLIDER (NEW)
        if (volumeSlider != null) {
            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                double volume = newVal.doubleValue() / 100.0;
                store.setSoundVolume(volume);
                SoundManager.setVolume(volume);
                statusLabel.setText("Volume: " + (int) newVal.doubleValue() + "%");
            });
        }

        // ⚙️ ALGORITHM
        algoDropdown.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                store.setHashAlgorithm(newVal);
                statusLabel.setText("Algorithm: " + newVal);
                SoundManager.play(SoundManager.Sound.BUTTON_CLICK);
            }
        });

        // 📁 SKIP HIDDEN
        skipHiddenCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            store.setSkipHidden(newVal);
            statusLabel.setText(newVal ? "Skipping hidden files" : "Including hidden files");
            SoundManager.play(SoundManager.Sound.BUTTON_CLICK);
        });

        // 🛡️ SKIP SYSTEM FILES (NEW)
        skipSystemFilesCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            store.setSkipSystemFiles(newVal);
            statusLabel.setText(newVal ? "System protection enabled" : "System protection disabled");
            SoundManager.play(SoundManager.Sound.BUTTON_CLICK);
        });

        // 📏 MIN SIZE
        minSizeInput.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                long value = Long.parseLong(newVal);
                store.setMinFileSizeKB(value);
                statusLabel.setText("Min size: " + value + " KB");
            } catch (Exception e) {
                // Invalid input, ignore
            }
        });
    }

    // ════════════════════════════════════════════════
    // UI UPDATE METHODS
    // ════════════════════════════════════════════════
    
    private void updateDarkButtonUI(boolean dark) {
        darkThemeToggle.setText(dark ? "ON" : "OFF");
        darkThemeToggle.setStyle(dark
                ? "-fx-background-color: #006565; -fx-text-fill: white; -fx-background-radius: 20;"
                : "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-background-radius: 20;");
    }

    private void applyTheme(boolean dark) {
        Scene scene = darkThemeToggle.getScene();
        if (scene != null) {
            scene.getStylesheets().clear();

            String css = dark 
                ? "/com/example/pr_1_file_dupe/CSS/dark-theme.css" 
                : "/com/example/pr_1_file_dupe/CSS/application.css";
            
            String cssPath = getClass().getResource(css).toExternalForm();
            scene.getStylesheets().add(cssPath);
        }
    }

    private void updateSafeModeUI(boolean safe) {
        safeModeToggle.setText(safe ? "ON" : "OFF");
        safeModeToggle.setStyle(safe
                ? "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 20;"
                : "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 20;");

        if (safeModeDesc != null) {
            safeModeDesc.setText(safe
                    ? "ON — Files moved to Trash (recoverable)"
                    : "OFF — Permanent delete ⚠");
        }
    }

    private void updateSoundButtonUI(boolean enabled) {
        soundToggle.setText(enabled ? "ON" : "OFF");
        soundToggle.setStyle(enabled
                ? "-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 20;"
                : "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 20;");
    }

    private void showWarning() {
        SoundManager.play(SoundManager.Sound.ERROR);
        Alert warn = new Alert(Alert.AlertType.WARNING);
        warn.setTitle("Warning");
        warn.setHeaderText("Permanent Delete Enabled");
        warn.setContentText("Files will be permanently deleted when you click delete. This cannot be undone.");
        warn.showAndWait();
    }
}