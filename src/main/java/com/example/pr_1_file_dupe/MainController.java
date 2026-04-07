package com.example.pr_1_file_dupe;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class MainController {

    // This matches the fx:id="contentArea" in your main.fxml
    @FXML
    private StackPane contentArea;

    // Optional: Load a default screen when the app starts
    @FXML
    public void initialize() {
        loadScreen("fxml/scan.fxml"); // Default screen
    }

    // --- Sidebar Button Handlers ---

    @FXML
    public void loadScan(ActionEvent event) {
        loadScreen("fxml/scan.fxml");
    }

    @FXML
    public void loadSettings(ActionEvent event) {
        loadScreen("fxml/setting.fxml");
    }

    @FXML
    public void loadResults(ActionEvent event) {
        loadScreen("fxml/result.fxml");
    }

    // --- Core Navigation Helper ---

    private void loadScreen(String fxmlFile) {
        try {
            // Locate the FXML file in the resources folder
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pr_1_file_dupe/" + fxmlFile));
            Parent screen = loader.load();

            // Clear the old screen and inject the new one
            contentArea.getChildren().clear();
            contentArea.getChildren().add(screen);

        } catch (IOException e) {
            System.err.println("❌ CRITICAL ERROR: Could not load " + fxmlFile);
            e.printStackTrace();
        }
    }
}