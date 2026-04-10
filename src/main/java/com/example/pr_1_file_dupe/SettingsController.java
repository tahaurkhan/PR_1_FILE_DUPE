package com.example.pr_1_file_dupe;

import com.example.pr_1_file_dupe.DataStore;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SettingsController {

    @FXML private ComboBox<String> algoDropdown;
    @FXML private CheckBox skipHiddenCheckbox;
    @FXML private TextField minSizeInput;
    @FXML private Label statusLabel;

    private DataStore store;

    @FXML
    public void initialize() {
        store = new DataStore();
        
        // 1. Load existing settings into the UI
        algoDropdown.setValue(store.getHashAlgorithm());
        skipHiddenCheckbox.setSelected(store.isSkipHidden());
        minSizeInput.setText(String.valueOf(store.getMinFileSizeKB()));
    }

    @FXML
    public void saveSettings() {
        // 2. Save the UI values back to the DataStore
        store.setHashAlgorithm(algoDropdown.getValue());
        store.setSkipHidden(skipHiddenCheckbox.isSelected());
        
        try {
            long minSize = Long.parseLong(minSizeInput.getText());
            store.setMinFileSizeKB(minSize);
        } catch (NumberFormatException e) {
            statusLabel.setText("Error: File size must be a number!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;"); // Red text for error
            return;
        }

        // Show success message
        statusLabel.setStyle("-fx-text-fill: #2ecc71;"); // Green text for success
        statusLabel.setText("Settings saved successfully!");
    }
}