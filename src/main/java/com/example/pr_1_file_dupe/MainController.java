package com.example.pr_1_file_dupe;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainController {

    @FXML 
    private BorderPane mainLayout;

    // A cache to remember our screens so they don't reset!
    private final Map<String, Parent> viewCache = new HashMap<>();

    // Keep reference to currently active button for highlight tracking
    private Button activeButton = null;

    // ═══════════════════════════════════════════════
    //  NAV BUTTON HANDLERS
    // ═══════════════════════════════════════════════
    
    @FXML
    public void showFiles(ActionEvent event) {
        setActive((Button) event.getSource());
        loadCachedScreen("/com/example/pr_1_file_dupe/fxml/dashboard.fxml");
    }

    @FXML
    public void showDuplicates(ActionEvent event) {
        setActive((Button) event.getSource());
        loadCachedScreen("/com/example/pr_1_file_dupe/fxml/dupelicates.fxml");
    }

    @FXML
    public void showCategories(ActionEvent event) {
        setActive((Button) event.getSource());

        // Make sure they actually ran a scan first!
        if (DashboardController.lastScanResults == null || DashboardController.lastScanResults.isEmpty()) {
            showError("Please run a scan from the Dashboard first!");
            return;
        }

        try {
            // For Categories, we load fresh instead of caching so the chart always updates with new data
            URL url = getClass().getResource("/com/example/pr_1_file_dupe/fxml/categories.fxml");
            if (url == null) {
                showError("categories.fxml not found.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent screen = loader.load();

            // Pass the data to the chart
            CategoriesController controller = loader.getController();
            controller.generateChart(DashboardController.lastScanResults);

            mainLayout.setCenter(screen);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading Categories screen: " + e.getMessage());
        }
    }

    @FXML
    public void showRecovery(ActionEvent event) {
        setActive((Button) event.getSource());
        loadCachedScreen("/com/example/pr_1_file_dupe/fxml/recovery.fxml");
    }

    @FXML
    public void openSettings(ActionEvent event) {
        setActive((Button) event.getSource());
        loadCachedScreen("/com/example/pr_1_file_dupe/fxml/setting.fxml");
    }

    // ═══════════════════════════════════════════════
    //  ACTIVE HIGHLIGHT
    // ═══════════════════════════════════════════════
    
    private void setActive(Button clicked) {
        // Remove active style from previous button
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-item-active");
            if (!activeButton.getStyleClass().contains("nav-item")) {
                activeButton.getStyleClass().add("nav-item");
            }
        }
        // Apply active style to clicked button
        clicked.getStyleClass().remove("nav-item");
        if (!clicked.getStyleClass().contains("nav-item-active")) {
            clicked.getStyleClass().add("nav-item-active");
        }

        activeButton = clicked;
    }

    // ═══════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════
    
    // Smart screen swapper that uses the cache
    private void loadCachedScreen(String fxmlPath) {
        try {
            // 1. Check if we already built this screen
            if (!viewCache.containsKey(fxmlPath)) {
                System.out.println("Loading " + fxmlPath + " for the first time...");
                
                URL url = getClass().getResource(fxmlPath);
                if (url == null) {
                    showError(fxmlPath + " not found. Check filename spelling.");
                    return;
                }
                
                FXMLLoader loader = new FXMLLoader(url);
                Parent screen = loader.load();
                
                // Save it to the cache so we never have to build it again!
                viewCache.put(fxmlPath, screen);
            }

            // 2. Set the center to our cached screen
            mainLayout.setCenter(viewCache.get(fxmlPath));

        } catch (IOException e) {
            System.out.println("Error swapping to screen: " + fxmlPath);
            e.printStackTrace();
            showError("Error loading screen: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Navigation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}