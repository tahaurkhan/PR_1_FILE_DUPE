package com.example.pr_1_file_dupe;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainController {

    @FXML private BorderPane mainLayout;
    @FXML private VBox sidebarPane;
    @FXML private Button hamburgerBtn;
    @FXML private Button btnFiles;
    @FXML private Button btnDuplicates;
    @FXML private Button btnCategories;
    @FXML private Button btnRecovery;
    @FXML private Button btnSettings;
    @FXML private StackPane contentPane;
    
    private Button activeButton = null;
    private boolean sidebarVisible = true;
    private double currentZoom = 1.0;
    
    // View Cache to make switching tabs instant
    private Map<String, Parent> viewCache = new HashMap<>();
    private Parent dashboardView = null;

    // IDE-STYLE ZOOM ENGINE
    private ScrollPane masterScrollPane = new ScrollPane();
    private Group zoomGroup = new Group();
    private StackPane centerWrapper = new StackPane();
    
    @FXML
    public void initialize() {
        DataStore store = new DataStore();
        com.example.pr_1_file_dupe.utils.SoundManager.setSoundEnabled(store.isSoundEnabled());
        com.example.pr_1_file_dupe.utils.SoundManager.setVolume(store.getSoundVolume());
    
        if (mainLayout.getScene() != null) {
            applyActiveTheme(mainLayout.getScene(), store.isDarkTheme());
        } else {
            mainLayout.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    applyActiveTheme(newScene, store.isDarkTheme());
                }
            });
        }
            
        // ASSEMBLE THE MASTER SCROLL WRAPPER
        centerWrapper.getChildren().add(zoomGroup);
        masterScrollPane.setContent(centerWrapper);
        masterScrollPane.setFitToWidth(true);
        masterScrollPane.setFitToHeight(true);
        masterScrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        showFiles(null);
    }
        
    // PLACES ALL SCREENS SAFELY INSIDE THE ZOOM ENGINE
    private void setMainContent(Parent view) {
        zoomGroup.getChildren().setAll(view);
        if (mainLayout.getCenter() != masterScrollPane) {
            mainLayout.setCenter(masterScrollPane);
        }
    }

    @FXML
    public void showFiles(ActionEvent e) {
        setActive(btnFiles);
        com.example.pr_1_file_dupe.utils.SoundManager.play(com.example.pr_1_file_dupe.utils.SoundManager.Sound.NAVIGATION);
            
        try {
            if (dashboardView == null) {
                dashboardView = new FXMLLoader(getClass().getResource("/com/example/pr_1_file_dupe/fxml/dashboard.fxml")).load();
            }
            setMainContent(dashboardView);
            applyZoom();
        } catch (IOException ex) {
            ex.printStackTrace();
            showError("Error loading Dashboard: " + ex.getMessage());
        }
    }

    @FXML 
    public void showDuplicates(ActionEvent e) {
        setActive(btnDuplicates);
        loadScreen("/com/example/pr_1_file_dupe/fxml/duplicate.fxml");
    }

    @FXML 
    public void showCategories(ActionEvent e) {
        if (DashboardController.lastScanResults == null || DashboardController.lastScanResults.isEmpty()) {
            showError("Please run a scan from the Dashboard first.");
            return; 
        }
        setActive(btnCategories);
        loadScreen("/com/example/pr_1_file_dupe/fxml/categories.fxml");
    }
    
    @FXML 
    public void showRecovery(ActionEvent e) {
        setActive(btnRecovery);
        loadScreen("/com/example/pr_1_file_dupe/fxml/recovery.fxml");
    }

    @FXML 
    public void openSetting(ActionEvent e) {
        setActive(btnSettings);
        loadScreen("/com/example/pr_1_file_dupe/fxml/settings.fxml");
    }

    // THE MASTER LOAD METHOD: Handles Caching, Sound, and Zoom
    private void loadScreen(String fxmlPath) {
        com.example.pr_1_file_dupe.utils.SoundManager.play(com.example.pr_1_file_dupe.utils.SoundManager.Sound.NAVIGATION);
        
        try {
            if (!viewCache.containsKey(fxmlPath)) {
                java.net.URL url = getClass().getResource(fxmlPath);
                if (url == null) { 
                    showError("File not found: " + fxmlPath); 
                    return; 
                }
                
                FXMLLoader loader = new FXMLLoader(url);
                Parent screen = loader.load();
                
                // Inject chart data if it's the categories screen
                if (fxmlPath.contains("categories")) {
                    CategoriesController controller = loader.getController();
                    if (controller != null && DashboardController.lastScanResults != null) {
                        controller.generateChart(DashboardController.lastScanResults);
                    }
                }
                
                viewCache.put(fxmlPath, screen);
            }
            
            Parent activeScreen = viewCache.get(fxmlPath);
            setMainContent(activeScreen);
            applyZoom();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("CRITICAL ERROR LOADING: " + fxmlPath);
            showError("Error loading screen: " + e.getMessage());
        }
    }

    @FXML
    public void toggleSidebar() {
        // Plays a clean click sound every time the sidebar changes size
        com.example.pr_1_file_dupe.utils.SoundManager.play(com.example.pr_1_file_dupe.utils.SoundManager.Sound.BUTTON_CLICK);

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
            if(btnFiles != null) btnFiles.setText(nowVisible ? "🗂  Files" : "🗂");
            if(btnDuplicates != null) btnDuplicates.setText(nowVisible ? "⧉   Duplicates" : "⧉");
            if(btnCategories != null) btnCategories.setText(nowVisible ? "📊  Categories" : "📊");
            if(btnRecovery != null) btnRecovery.setText(nowVisible ? "♻  Recovery" : "♻");
            if(btnSettings != null) btnSettings.setText(nowVisible ? "⚙  Settings" : "⚙");
        });
        sidebarVisible = !sidebarVisible;
        timeline.play();
    }

    @FXML public void menuOpenFolder() { showFiles(null); }
    @FXML public void menuNewScan() { showFiles(null); }
    @FXML public void menuSelectAll() { System.out.println("Select All clicked"); }
    @FXML public void menuDeselectAll() { System.out.println("Deselect All clicked"); }
    @FXML public void menuDeleteSelected() { System.out.println("Delete Selected clicked"); }

    @FXML public void menuQuit() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Quit");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to quit?");
        confirm.showAndWait().ifPresent(btn -> { if (btn == javafx.scene.control.ButtonType.OK) javafx.application.Platform.exit(); });
    }

    @FXML public void menuZoomIn() { currentZoom = Math.min(currentZoom + 0.1, 2.0); applyZoom(); }
    @FXML public void menuZoomOut() { currentZoom = Math.max(currentZoom - 0.1, 0.6); applyZoom(); }
    @FXML public void menuZoomReset() { currentZoom = 1.0; applyZoom(); }

    private void applyZoom() {
        if (!zoomGroup.getChildren().isEmpty()) {
            javafx.scene.Node view = zoomGroup.getChildren().get(0);
            view.setScaleX(currentZoom);
            view.setScaleY(currentZoom);
        }
    }

    @FXML public void menuAbout() {
        try {
            java.net.URL aboutUrl = getClass().getResource("/com/example/pr_1_file_dupe/fxml/about.fxml");
            if (aboutUrl != null) {
                Parent view = new FXMLLoader(aboutUrl).load();
                setMainContent(view);
                applyZoom();
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
        try {
            String mailto = "mailto:x.tahaur@gmail.com,guptapraveen67984@gmail.com?subject=Bug%20Report";
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec("cmd /c start " + mailto);
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open " + mailto);
            } else {
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
        if (clicked == null) return;
        
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-item-active");
            if (!activeButton.getStyleClass().contains("nav-item")) activeButton.getStyleClass().add("nav-item");
        }
        clicked.getStyleClass().remove("nav-item");
        if (!clicked.getStyleClass().contains("nav-item-active")) clicked.getStyleClass().add("nav-item-active");
        activeButton = clicked;
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Notice");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void applyActiveTheme(javafx.scene.Scene scene, boolean dark) {
        if (scene != null) {
            scene.getStylesheets().clear();
            if (dark) {
                scene.getStylesheets().add(getClass().getResource("/com/example/pr_1_file_dupe/CSS/dark-theme.css").toExternalForm());
            } else {
                scene.getStylesheets().add(getClass().getResource("/com/example/pr_1_file_dupe/CSS/application.css").toExternalForm());
            }
        }
    }
}
