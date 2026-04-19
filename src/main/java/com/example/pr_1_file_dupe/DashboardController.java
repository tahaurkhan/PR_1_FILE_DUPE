package com.example.pr_1_file_dupe;

import com.example.pr_1_file_dupe.service.EnhancedFileScanner;
import com.example.pr_1_file_dupe.utils.SoundManager;
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

/**
 * Enhanced DashboardController with Sound Effects
 * Integrates EnhancedFileScanner and SoundManager
 */
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

        // Load sounds based on user preference
        SoundManager.setSoundEnabled(store.isSoundEnabled());
        SoundManager.setVolume(store.getSoundVolume());

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
        // Play navigation sound
        SoundManager.playAsync(SoundManager.Sound.BUTTON_CLICK);

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
            
            // Play success sound
            SoundManager.playAsync(SoundManager.Sound.SUCCESS);
        }
    }

    @FXML
    public void startScan(ActionEvent event) {
        String targetFolder = pathInputField.getText();

        if (targetFolder == null || targetFolder.trim().isEmpty()) {
            // Play error sound
            SoundManager.playAsync(SoundManager.Sound.ERROR);
            
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Path");
            alert.setHeaderText(null);
            alert.setContentText("Please enter or browse a folder path first.");
            alert.showAndWait();
            return;
        }

        // Play scan start sound
        SoundManager.playAsync(SoundManager.Sound.SCAN_START);

        new DataStore().setLastFolder(targetFolder);

        loadingBox.setVisible(true);
        scanButton.setDisable(true);
        System.out.println("🔍 Initializing enhanced scanner for: " + targetFolder);

        Task<Map<String, List<FileData>>> scanTask = new Task<>() {
            @Override
            protected Map<String, List<FileData>> call() throws Exception {
                // Use EnhancedFileScanner instead of FileScanner
                EnhancedFileScanner scanner = new EnhancedFileScanner();
                List<FileData> scannedFiles = scanner.scanDirectory(targetFolder);
                
                // Log scan statistics
                System.out.println("═══ ENHANCED SCAN STATS ═══");
                System.out.println("System files protected: " + scanner.getSystemFilesSkipped());
                System.out.println("Locked files skipped: " + scanner.getLockedFilesSkipped());
                System.out.println("Total files scanned: " + scannedFiles.size());
                
                return new DuplicateFinder().findDuplicates(scannedFiles);
            }
        };

        scanTask.setOnSucceeded(e -> {
            Map<String, List<FileData>> duplicates = scanTask.getValue();
            lastScanResults = duplicates;

            // Save stats
            int totalFiles = duplicates.values().stream()
                    .mapToInt(List::size).sum();
            new DataStore().updateStats(0, duplicates.size(), totalFiles);

            // Refresh stat cards
            initialize();

            loadingBox.setVisible(false);
            scanButton.setDisable(false);

            // Play scan complete sound
            SoundManager.playAsync(SoundManager.Sound.SCAN_COMPLETE);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/example/pr_1_file_dupe/fxml/dupelicate.fxml"));
                Parent duplicatesScreen = loader.load();

                BorderPane mainLayout = (BorderPane) pathInputField.getScene().getRoot();
                mainLayout.setCenter(duplicatesScreen);

                // Show completion message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Scan Complete");
                alert.setHeaderText(null);
                alert.setContentText("✓ Found " + duplicates.size() + " duplicate groups\n" +
                                   "📁 Total files: " + totalFiles + "\n" +
                                   "🛡️ System files protected");
                alert.showAndWait();

            } catch (Exception ex) {
                ex.printStackTrace();
                SoundManager.playAsync(SoundManager.Sound.ERROR);
                new Alert(Alert.AlertType.ERROR, "Could not load duplicates view: "
                        + ex.getMessage()).showAndWait();
            }
        });

        scanTask.setOnFailed(e -> {
            loadingBox.setVisible(false);
            scanButton.setDisable(false);
            
            // Play error sound
            SoundManager.playAsync(SoundManager.Sound.ERROR);
            
            new Alert(Alert.AlertType.ERROR, "Scan failed: "
                    + scanTask.getException().getMessage()).showAndWait();
        });

        Thread t = new Thread(scanTask);
        t.setDaemon(true);
        t.start();
    }
}