package com.example.pr_1_file_dupe;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

public class MainController {

    @FXML private BorderPane mainLayout;
    @FXML private VBox       sidebarPane;
    @FXML private Button     hamburgerBtn;
    @FXML private Button     btnFiles;
    @FXML private Button     btnDuplicates;
    @FXML private Button     btnCategories;
    @FXML private Button     btnRecovery;
    @FXML private Button     btnSettings;
    @FXML private StackPane  contentPane;

    private Button  activeButton   = null;
    private boolean sidebarVisible = true;
    private double  currentZoom    = 1.0;

    @FXML
    public void initialize() {
        javafx.application.Platform.runLater(() ->
                ThemeManager.apply(mainLayout.getScene()));
        showFiles(null);
    }

    @FXML public void showFiles(ActionEvent e) {
        setActive(btnFiles);
        loadScreen("/com/example/pr_1_file_dupe/fxml/dashboard.fxml");
    }

    @FXML public void showDuplicates(ActionEvent e) {
        setActive(btnDuplicates);
        java.net.URL url = getClass().getResource("/com/example/pr_1_file_dupe/fxml/dupelicate.fxml");
        if (url == null) { showError("dupelicate.fxml not found."); return; }
        try { mainLayout.setCenter(new FXMLLoader(url).load()); } 
        catch (Exception ex) { ex.printStackTrace(); showError("Error loading Duplicates: " + ex.getMessage()); }
    }

    @FXML public void showCategories(ActionEvent e) {
        setActive(btnCategories);
        if (DashboardController.lastScanResults == null || DashboardController.lastScanResults.isEmpty()) {
            showError("Please run a scan from the Dashboard first.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pr_1_file_dupe/fxml/categories.fxml"));
            Parent screen = loader.load();
            ((CategoriesController) loader.getController()).generateChart(DashboardController.lastScanResults);
            mainLayout.setCenter(screen);
        } catch (Exception ex) { showError("Error loading Categories: " + ex.getMessage()); }
    }

    @FXML public void showRecovery(ActionEvent e) {
        setActive(btnRecovery);
        loadScreen("/com/example/pr_1_file_dupe/fxml/recovery.fxml");
    }

    @FXML public void openSetting(ActionEvent e) {
        setActive(btnSettings);
        try {
            java.net.URL settingsUrl = getClass().getResource("/com/example/pr_1_file_dupe/fxml/settings.fxml");
            if (settingsUrl == null) { showError("settings.fxml not found."); return; }
            FXMLLoader loader = new FXMLLoader(settingsUrl);
            mainLayout.setCenter(loader.load());
        } catch (IOException ex) { ex.printStackTrace(); showError("Error loading Settings: " + ex.getMessage()); }
    }

    @FXML
    public void toggleSidebar() {
        // Shrink to 45px instead of 0 so the hamburger menu stays visible
        double targetWidth = sidebarVisible ? 45 : 170;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(200),
                        new KeyValue(sidebarPane.prefWidthProperty(), targetWidth),
                        new KeyValue(sidebarPane.minWidthProperty(), targetWidth),
                        new KeyValue(sidebarPane.maxWidthProperty(), targetWidth)
                )
        );

        timeline.setOnFinished(ev -> {
            boolean nowVisible = targetWidth > 45;
            btnFiles.setText(nowVisible      ? "🗂  Files"       : "🗂");
            btnDuplicates.setText(nowVisible ? "⧉   Duplicates"  : "⧉");
            btnCategories.setText(nowVisible ? "📊  Categories"  : "📊");
            btnRecovery.setText(nowVisible   ? "♻  Recovery"    : "♻");
            btnSettings.setText(nowVisible   ? "⚙  Settings"    : "⚙");
        });

        sidebarVisible = !sidebarVisible;
        timeline.play();
    }

    @FXML public void menuOpenFolder() { showFiles(null); }
    @FXML public void menuNewScan() { showFiles(null); }
    @FXML public void menuQuit() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Quit");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to quit?");
        confirm.showAndWait().ifPresent(btn -> { if (btn == javafx.scene.control.ButtonType.OK) javafx.application.Platform.exit(); });
    }

    @FXML public void menuSelectAll() { }
    @FXML public void menuDeselectAll() { }
    @FXML public void menuDeleteSelected() { }

    @FXML public void menuZoomIn() { currentZoom = Math.min(currentZoom + 0.1, 2.0); applyZoom(); }
    @FXML public void menuZoomOut() { currentZoom = Math.max(currentZoom - 0.1, 0.6); applyZoom(); }
    @FXML public void menuZoomReset() { currentZoom = 1.0; applyZoom(); }

    private void applyZoom() {
        if (mainLayout.getCenter() != null) {
            mainLayout.getCenter().setScaleX(currentZoom);
            mainLayout.getCenter().setScaleY(currentZoom);
        }
    }

    @FXML public void menuAbout() {
        try {
            java.net.URL aboutUrl = getClass().getResource("/com/example/pr_1_file_dupe/fxml/about.fxml");
            if (aboutUrl != null) {
                mainLayout.setCenter(new FXMLLoader(aboutUrl).load());
            } else {
                Alert about = new Alert(Alert.AlertType.INFORMATION);
                about.setTitle("About");
                about.setHeaderText("Duplicate File Detector  v1.0");
                about.setContentText("A smart tool to find and remove duplicate files.");
                about.showAndWait();
            }
        } catch (IOException ex) { showError("Error loading About screen."); }
    }

    @FXML
    public void menuReportBug() {
        // 🔥 FIXED: Check OS to prevent GTK error trap crash on Linux
        try {
            String mailto = "mailto:x.tahaur@gmail.com,guptapraveen67984@gmail.com?subject=Bug%20Report";
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                Runtime.getRuntime().exec("cmd /c start " + mailto);
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open " + mailto);
            } else {
                // Safely copy to clipboard on Linux
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString("x.tahaur@gmail.com, guptapraveen67984@gmail.com");
                javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
                showError("Direct mail opening is restricted on Linux. We copied the support email to your clipboard!");
            }
        } catch (Exception e) {
            showError("Could not open email client. Please email us at:\nx.tahaur@gmail.com");
        }
    }

    private void setActive(Button clicked) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-item-active");
            if (!activeButton.getStyleClass().contains("nav-item")) activeButton.getStyleClass().add("nav-item");
        }
        clicked.getStyleClass().remove("nav-item");
        if (!clicked.getStyleClass().contains("nav-item-active")) clicked.getStyleClass().add("nav-item-active");
        activeButton = clicked;
    }

    private void loadScreen(String fxmlPath) {
        try { mainLayout.setCenter(new FXMLLoader(getClass().getResource(fxmlPath)).load()); } 
        catch (IOException e) { showError("Error loading screen: " + fxmlPath); }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Notice");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}