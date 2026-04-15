package com.example.pr_1_file_dupe;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SettingsController {

    @FXML private ComboBox<String> algoDropdown;
    @FXML private CheckBox         skipHiddenCheckbox;
    @FXML private TextField        minSizeInput;
    @FXML private Label            statusLabel;
    @FXML private ToggleButton     darkThemeToggle;
    @FXML private ToggleButton     safeModeToggle;
    @FXML private Label            safeModeDesc;

    private DataStore store;

    @FXML
    public void initialize() {
        store = new DataStore();

        // Existing settings
        algoDropdown.setValue(store.getHashAlgorithm());
        skipHiddenCheckbox.setSelected(store.isSkipHidden());
        minSizeInput.setText(String.valueOf(store.getMinFileSizeKB()));

        // Dark theme toggle
        boolean dark = store.isDarkTheme();
        darkThemeToggle.setSelected(dark);
        darkThemeToggle.setText(dark ? "ON" : "OFF");
        darkThemeToggle.setStyle(dark
                ? "-fx-background-color: #006565; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 5 18; -fx-font-weight: bold;"
                : "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-background-radius: 20; -fx-padding: 5 18; -fx-font-weight: bold;"
        );

        // Safe mode toggle
        boolean safe = store.isSafeMode();
        safeModeToggle.setSelected(safe);
        updateSafeModeUI(safe);
    }

    @FXML
    public void toggleDarkTheme() {
        boolean dark = darkThemeToggle.isSelected();
        store.setDarkTheme(dark);

        // Update button appearance
        darkThemeToggle.setText(dark ? "ON" : "OFF");
        darkThemeToggle.setStyle(dark
                ? "-fx-background-color: #006565; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 5 18; -fx-font-weight: bold;"
                : "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-background-radius: 20; -fx-padding: 5 18; -fx-font-weight: bold;"
        );

        // ✅ Apply theme instantly to whole app — no restart needed
        javafx.stage.Stage stage = (javafx.stage.Stage)
                darkThemeToggle.getScene().getWindow();
        ThemeManager.apply(stage.getScene());

        statusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        statusLabel.setText(dark ? "Dark theme enabled!" : "Light theme enabled!");
    }

    @FXML
    public void toggleSafeMode() {
        boolean safe = safeModeToggle.isSelected();
        store.setSafeMode(safe);
        updateSafeModeUI(safe);

        // Warn user when turning safe mode OFF
        if (!safe) {
            Alert warn = new Alert(Alert.AlertType.WARNING);
            warn.setTitle("Safe Mode Disabled");
            warn.setHeaderText("⚠ Permanent deletion enabled");
            warn.setContentText(
                    "Safe mode is OFF.\n\n" +
                    "Files will be PERMANENTLY deleted — they cannot be recovered.\n\n" +
                    "Are you sure you want to continue?");
            warn.showAndWait();
        }

        statusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        statusLabel.setText(safe ? "Safe mode ON — files go to Trash."
                                 : "⚠ Safe mode OFF — permanent delete active.");
    }

    private void updateSafeModeUI(boolean safe) {
        safeModeToggle.setText(safe ? "ON" : "OFF");
        safeModeToggle.setStyle(safe
                ? "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 5 18; -fx-font-weight: bold;"
                : "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 5 18; -fx-font-weight: bold;"
        );
        safeModeDesc.setText(safe
                ? "ON — Files moved to Trash (recoverable)"
                : "OFF — Files permanently deleted ⚠");
        safeModeDesc.setStyle(safe
                ? "-fx-text-fill: #27ae60; -fx-font-size: 12px;"
                : "-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-font-weight: bold;");
    }

    @FXML
    public void saveSettings() {
        store.setHashAlgorithm(algoDropdown.getValue());
        store.setSkipHidden(skipHiddenCheckbox.isSelected());
        try {
            store.setMinFileSizeKB(Long.parseLong(minSizeInput.getText()));
        } catch (NumberFormatException e) {
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            statusLabel.setText("Error: File size must be a number!");
            return;
        }
        statusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        statusLabel.setText("Settings saved successfully!");
    }
}