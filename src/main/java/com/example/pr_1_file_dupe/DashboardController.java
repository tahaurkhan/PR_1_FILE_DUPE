package com.example.pr_1_file_dupe;

import com.example.pr_1_file_dupe.service.FileScanner;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class DashboardController {

    @FXML private TextField pathInputField;
    @FXML private Button    scanButton;
    @FXML private VBox      loadingBox;
    @FXML private Label     totalSavedLabel;
    @FXML private Label     groupsFoundLabel;
    @FXML private Label     scannedCountLabel;

    public static Map<String, List<FileData>> lastScanResults;
    private Tooltip pathTooltip;

    @FXML
    public void initialize() {
        DataStore store = new DataStore();

        // Load and display saved folder path
        String lastFolder = store.getLastFolder();
        if (lastFolder != null && !lastFolder.isEmpty()) {
            pathInputField.setText(lastFolder);
        }

        // Add tooltip
        pathTooltip = new Tooltip();
        pathTooltip.setShowDelay(javafx.util.Duration.seconds(1));
        Tooltip.install(pathInputField, pathTooltip);
        
        pathInputField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                pathTooltip.setText(newVal);
            }
        });

        // Display stats
        long savedBytes = Long.parseLong(store.getTotalSaved());
        String formattedSize = (savedBytes >= 1024L * 1024 * 1024)
                ? String.format("%.1f GB", savedBytes / (1024.0 * 1024 * 1024))
                : (savedBytes >= 1024 * 1024)
                ? String.format("%.1f MB", savedBytes / (1024.0 * 1024))
                : String.format("%.1f KB", savedBytes / 1024.0);

        totalSavedLabel.setText(formattedSize);
        groupsFoundLabel.setText(store.getTotalGroups());
        scannedCountLabel.setText(store.getTotalScanned());
    }

    @FXML
    public void browseFolder() {
        javafx.stage.DirectoryChooser chooser = new javafx.stage.DirectoryChooser();
        chooser.setTitle("Select Folder to Scan");
        
        DataStore store = new DataStore();
        String lastFolder = store.getLastFolder();
        if (lastFolder != null && !lastFolder.isEmpty()) {
            java.io.File lastDir = new java.io.File(lastFolder);
            if (lastDir.exists() && lastDir.isDirectory()) {
                chooser.setInitialDirectory(lastDir);
            } else {
                chooser.setInitialDirectory(new java.io.File(System.getProperty("user.home")));
            }
        } else {
            chooser.setInitialDirectory(new java.io.File(System.getProperty("user.home")));
        }

        java.io.File selected = chooser.showDialog(pathInputField.getScene().getWindow());
        if (selected != null) {
            pathInputField.setText(selected.getAbsolutePath());
            store.setLastFolder(selected.getAbsolutePath());
        }
    }

    @FXML
    public void startScan(ActionEvent event) {
        String targetFolder = pathInputField.getText();

        if (targetFolder == null || targetFolder.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Path");
            alert.setHeaderText(null);
            alert.setContentText("Please enter or browse a folder path first.");
            alert.showAndWait();
            return;
        }

        new DataStore().setLastFolder(targetFolder);

        loadingBox.setVisible(true);
        scanButton.setDisable(true);
        System.out.println("🔍 Initializing scanner for: " + targetFolder);

        //    add sound program
        
        com.example.pr_1_file_dupe.utils.SoundManager.play(com.example.pr_1_file_dupe.utils.SoundManager.Sound.SCAN_START);
        Task<Map<String, List<FileData>>> scanTask = new Task<>() {
            @Override
            protected Map<String, List<FileData>> call() throws Exception {
                FileScanner scanner = new FileScanner();
                List<FileData> scannedFiles = scanner.scanDirectory(targetFolder);
                return new DuplicateFinder().findDuplicates(scannedFiles);
            }
        };

        scanTask.setOnSucceeded(e -> handleScanSuccess(scanTask));
        scanTask.setOnFailed(e -> handleScanFailure(scanTask));

        Thread t = new Thread(scanTask);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void startFullSystemScan(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Full System Scan");
        confirm.setHeaderText("Scan All Drives?");
        confirm.setContentText(
            "This will scan ALL accessible files on your computer.\n\n" +
            "• First scan: 10-30 minutes (builds hash cache)\n" +
            "• Next scans: MUCH faster (uses cached hashes)\n" +
            "• Skips system folders and locked files\n\n" +
            "Continue?"
        );
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        
        loadingBox.setVisible(true);
        scanButton.setDisable(true);
        System.out.println("🌐 Initializing FULL SYSTEM scan...");

        Task<Map<String, List<FileData>>> scanTask = new Task<>() {
            @Override
            protected Map<String, List<FileData>> call() throws Exception {
                FileScanner scanner = new FileScanner();
                List<FileData> scannedFiles = scanner.scanFullSystem();
                return new DuplicateFinder().findDuplicates(scannedFiles);
            }
        };

        scanTask.setOnSucceeded(e -> {
            Map<String, List<FileData>> duplicates = scanTask.getValue();
            lastScanResults = duplicates;

            int totalFiles = duplicates.values().stream()
                    .mapToInt(List::size).sum();
            new DataStore().updateStats(0, duplicates.size(), totalFiles);

            initialize();

            loadingBox.setVisible(false);
            scanButton.setDisable(false);

            // Show completion popup first
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("System Scan Complete");
            alert.setHeaderText("✅ Full system scan complete!");
            alert.setContentText(
                "Found " + duplicates.size() + " duplicate groups\n" +
                "Total files: " + totalFiles + "\n\n" +
                "💡 Next scan will be MUCH faster!"
            );
            alert.showAndWait();

            // 🔥 SAFELY SWITCH SCREENS USING YOUR MAIN CONTROLLER
            try {
                javafx.scene.layout.BorderPane root = (javafx.scene.layout.BorderPane) pathInputField.getScene().getRoot();
                javafx.scene.control.Button btnDup = (javafx.scene.control.Button) root.lookup("#btnDuplicates");
                
                if (btnDup != null) {
                    btnDup.fire(); // This safely triggers MainController.showDuplicates()
                }
            } catch (Exception ex) {
                System.out.println("Auto-switch failed, but scan data is saved.");
            }
        });	

        scanTask.setOnFailed(e -> handleScanFailure(scanTask));

        Thread t = new Thread(scanTask);
        t.setDaemon(true);
        t.start();
    }

    private void handleScanSuccess(Task<Map<String, List<FileData>>> scanTask) {
        Map<String, List<FileData>> duplicates = scanTask.getValue();
        lastScanResults = duplicates;

        // Update session stats
        int totalFiles = duplicates.values().stream().mapToInt(List::size).sum();
        new DataStore().updateStats(0, duplicates.size(), totalFiles);

        initialize();
        loadingBox.setVisible(false);
        scanButton.setDisable(false);

        // Play completion sound
        com.example.pr_1_file_dupe.utils.SoundManager.play(com.example.pr_1_file_dupe.utils.SoundManager.Sound.SCAN_COMPLETE);

        // 🔥 AUTOMATIC SWITCH: Access the MainController and trigger the Duplicates button
        try {
            // Find the root BorderPane
            javafx.scene.layout.BorderPane root = (javafx.scene.layout.BorderPane) pathInputField.getScene().getRoot();
            
            // Find the btnDuplicates from the sidebar
            // This assumes btnDuplicates is defined in your main.fxml
            javafx.scene.control.Button btnDup = (javafx.scene.control.Button) root.lookup("#btnDuplicates");
            
            if (btnDup != null) {
                // Programmatically click the button to trigger logic in MainController
                btnDup.fire();
            }
        } catch (Exception ex) {
            System.out.println("Auto-switch failed, but scan data is saved.");
        }
    }   
    

    private void handleScanFailure(Task<Map<String, List<FileData>>> scanTask) {
        loadingBox.setVisible(false);
        scanButton.setDisable(false);
        
        Throwable ex = scanTask.getException();
        ex.printStackTrace();
        
        new Alert(Alert.AlertType.ERROR, 
            "Scan failed: " + ex.getMessage()).showAndWait();
    }
}