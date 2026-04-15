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
                "/com/example/pr_1_file_dupe/fxml/duplicates.fxml");
        if (url == null) { showError("duplicates.fxml not found."); return; }
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

    @FXML public void openSettings(ActionEvent e) {
        setActive(btnSettings);
        loadScreen("/com/example/pr_1_file_dupe/fxml/setting.fxml");
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
        // Switch to dashboard then trigger browse
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
    //  MENU — VIEW  (Zoom)
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

    private void applyZoom() {
        mainLayout.setScaleX(currentZoom);
        mainLayout.setScaleY(currentZoom);
    }

    // ═══════════════════════════════════════════════
    //  MENU — ABOUT
    // ═══════════════════════════════════════════════
    @FXML
    public void menuAbout() {
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle("About");
        about.setHeaderText("Duplicate File Detector  v1.0");
        about.setContentText(
                "A smart tool to find and remove duplicate files.\n\n" +
                "Built with Java 25 + JavaFX 21\n" +
                "Author: Your Name\n\n" +
                "Shortcuts:\n" +
                "  Ctrl+O  →  Open Folder\n" +
                "  Ctrl+Q  →  Quit\n" +
                "  Ctrl+B  →  Toggle Sidebar\n" +
                "  Ctrl++  →  Zoom In\n" +
                "  Ctrl+-  →  Zoom Out\n" +
                "  Ctrl+0  →  Reset Zoom");
        about.showAndWait();
    }

    @FXML
    public void menuReportBug() {
        try {
            java.awt.Desktop.getDesktop().browse(
                    new java.net.URI("https://github.com/yourname/duplicate-finder/issues"));
        } catch (Exception e) {
            showError("Could not open browser.");
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