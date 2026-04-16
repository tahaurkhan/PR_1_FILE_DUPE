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

    // ═══════════════════════════════════════════════
    //  INITIALIZE
    // ═══════════════════════════════════════════════
    @FXML
    public void initialize() {
        // Apply saved theme on launch
        javafx.application.Platform.runLater(() ->
                ThemeManager.apply(mainLayout.getScene()));
        
        // Load dashboard by default
        showFiles(null);
    }

    // ═══════════════════════════════════════════════
    //  NAV BUTTONS
    // ═══════════════════════════════════════════════
    @FXML public void showFiles(ActionEvent e) {
        setActive(btnFiles);
        loadScreen("/com/example/pr_1_file_dupe/fxml/dashboard.fxml");
    }

    @FXML public void showDuplicates(ActionEvent e) {
        setActive(btnDuplicates);
        java.net.URL url = getClass().getResource(
                "/com/example/pr_1_file_dupe/fxml/dupelicate.fxml");
        if (url == null) { showError("dupelicate.fxml not found."); return; }
        try {
            mainLayout.setCenter(new FXMLLoader(url).load());
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Error loading Duplicates: " + ex.getMessage());
        }
    }

    @FXML public void showCategories(ActionEvent e) {
        setActive(btnCategories);
        if (DashboardController.lastScanResults == null
                || DashboardController.lastScanResults.isEmpty()) {
            showError("Please run a scan from the Dashboard first.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/pr_1_file_dupe/fxml/categories.fxml"));
            Parent screen = loader.load();
            ((CategoriesController) loader.getController())
                    .generateChart(DashboardController.lastScanResults);
            mainLayout.setCenter(screen);
        } catch (Exception ex) {
            showError("Error loading Categories: " + ex.getMessage());
        }
    }

    @FXML public void showRecovery(ActionEvent e) {
        setActive(btnRecovery);
        loadScreen("/com/example/pr_1_file_dupe/fxml/recovery.fxml");
    }

    @FXML public void openSetting(ActionEvent e) {
        setActive(btnSettings);
        try {
            java.net.URL settingsUrl = getClass().getResource(
                    "/com/example/pr_1_file_dupe/fxml/settings.fxml");
            
            if (settingsUrl == null) {
                showError("settings.fxml not found in resources.\nPlease ensure the file exists at:\nsrc/main/resources/com/example/pr_1_file_dupe/fxml/settings.fxml");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(settingsUrl);
            Parent settingsScreen = loader.load();
            mainLayout.setCenter(settingsScreen);
            
        } catch (IOException ex) {
            ex.printStackTrace();
            showError("Error loading Settings: " + ex.getMessage());
        }
    }

    // ═══════════════════════════════════════════════
    //  SIDEBAR TOGGLE  (Ctrl+B)
    // ═══════════════════════════════════════════════
    @FXML
    public void toggleSidebar() {
        double targetWidth = sidebarVisible ? 0 : 170;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(200),
                        new KeyValue(sidebarPane.prefWidthProperty(), targetWidth),
                        new KeyValue(sidebarPane.minWidthProperty(), targetWidth),
                        new KeyValue(sidebarPane.maxWidthProperty(), targetWidth)
                )
        );

        // Hide text labels when collapsed, show when expanded
        timeline.setOnFinished(ev -> {
            boolean nowVisible = targetWidth > 0;
            btnFiles.setText(nowVisible      ? "🗂  Files"       : "🗂");
            btnDuplicates.setText(nowVisible ? "🔁  Duplicates"  : "🔁");
            btnCategories.setText(nowVisible ? "📊  Categories"  : "📊");
            btnRecovery.setText(nowVisible   ? "♻  Recovery"    : "♻");
            btnSettings.setText(nowVisible   ? "⚙  Settings"    : "⚙");
        });

        sidebarVisible = !sidebarVisible;
        timeline.play();
    }

    // ═══════════════════════════════════════════════
    //  MENU — FILE
    // ═══════════════════════════════════════════════
    @FXML
    public void menuOpenFolder() {
        setActive(btnFiles);
        loadScreen("/com/example/pr_1_file_dupe/fxml/dashboard.fxml");
    }

    @FXML
    public void menuNewScan() {
        setActive(btnFiles);
        loadScreen("/com/example/pr_1_file_dupe/fxml/dashboard.fxml");
    }

    @FXML
    public void menuQuit() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Quit");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to quit?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == javafx.scene.control.ButtonType.OK)
                javafx.application.Platform.exit();
        });
    }

    // ═══════════════════════════════════════════════
    //  MENU — EDIT
    // ═══════════════════════════════════════════════
    @FXML public void menuSelectAll()      { System.out.println("Select All — wire to DuplicatesController"); }
    @FXML public void menuDeselectAll()    { System.out.println("Deselect All"); }
    @FXML public void menuDeleteSelected() { System.out.println("Delete Selected"); }

    // ═══════════════════════════════════════════════
    //  MENU — VIEW  (Zoom - FIXED to zoom only content)
    // ═══════════════════════════════════════════════
    @FXML
    public void menuZoomIn() {
        currentZoom = Math.min(currentZoom + 0.1, 2.0);
        applyZoom();
    }

    @FXML
    public void menuZoomOut() {
        currentZoom = Math.max(currentZoom - 0.1, 0.6);
        applyZoom();
    }

    @FXML
    public void menuZoomReset() {
        currentZoom = 1.0;
        applyZoom();
    }

    // 🔥 FIXED: Only zoom the content area, not menu bar
    private void applyZoom() {
        if (mainLayout.getCenter() != null) {
            mainLayout.getCenter().setScaleX(currentZoom);
            mainLayout.getCenter().setScaleY(currentZoom);
        }
    }

    // ═══════════════════════════════════════════════
    //  MENU — ABOUT
    // ═══════════════════════════════════════════════
    @FXML
    public void menuAbout() {
        try {
            java.net.URL aboutUrl = getClass().getResource(
                    "/com/example/pr_1_file_dupe/fxml/about.fxml");
            
            if (aboutUrl != null) {
                FXMLLoader loader = new FXMLLoader(aboutUrl);
                Parent aboutScreen = loader.load();
                mainLayout.setCenter(aboutScreen);
            } else {
                // Fallback to simple Alert dialog
                Alert about = new Alert(Alert.AlertType.INFORMATION);
                about.setTitle("About");
                about.setHeaderText("Duplicate File Detector  v1.0");
                about.setContentText(
                        "A smart tool to find and remove duplicate files.\n\n" +
                        "Built with Java 25 + JavaFX 21\n\n" +
                        "Developers:\n" +
                        "• Tahaur Nazrul Islam Khan\n" +
                        "  Email: x.tahaur@gmail.com\n" +
                        "  Phone: +91 9326964930\n\n" +
                        "• Gupta Praveen\n" +
                        "  Email: guptapraveen67984@gmail.com\n" +
                        "  Phone: +91 744 753 4511");
                about.showAndWait();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            showError("Error loading About screen: " + ex.getMessage());
        }
    }

    // 🔥 FIXED: Open email client instead of browser
    @FXML
    public void menuReportBug() {
        try {
            String subject = "Bug Report - Duplicate File Detector";
            String body = "Please describe the bug you encountered:\n\n";
            String mailto = "mailto:x.tahaur@gmail.com,guptapraveen67984@gmail.com"
                    + "?subject=" + java.net.URLEncoder.encode(subject, "UTF-8")
                    + "&body=" + java.net.URLEncoder.encode(body, "UTF-8");
            
            java.awt.Desktop.getDesktop().mail(new java.net.URI(mailto));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not open email client. Please email us at:\nx.tahaur@gmail.com\nguptapraveen67984@gmail.com");
        }
    }

    // ═══════════════════════════════════════════════
    //  ACTIVE NAV HIGHLIGHT
    // ═══════════════════════════════════════════════
    private void setActive(Button clicked) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-item-active");
            if (!activeButton.getStyleClass().contains("nav-item"))
                activeButton.getStyleClass().add("nav-item");
        }
        clicked.getStyleClass().remove("nav-item");
        if (!clicked.getStyleClass().contains("nav-item-active"))
            clicked.getStyleClass().add("nav-item-active");
        activeButton = clicked;
    }

    // ═══════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════
    private void loadScreen(String fxmlPath) {
        try {
            mainLayout.setCenter(
                    new FXMLLoader(getClass().getResource(fxmlPath)).load());
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading screen: " + fxmlPath);
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Notice");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}