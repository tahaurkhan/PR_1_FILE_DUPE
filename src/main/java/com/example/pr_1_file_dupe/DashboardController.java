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

        // ✅ Load and display saved folder path
        String lastFolder = store.getLastFolder();
        if (lastFolder != null && !lastFolder.isEmpty()) {
            pathInputField.setText(lastFolder);
        }

        // ✅ Add tooltip that shows full path on hover after 1 second
        pathTooltip = new Tooltip();
        pathTooltip.setShowDelay(javafx.util.Duration.seconds(1));
        Tooltip.install(pathInputField, pathTooltip);
        
        // Update tooltip text whenever path changes
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
        
        // ✅ Start from last used folder if available
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
            // ✅ Save the selected folder path
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

        // ✅ Save the folder path before scanning
        new DataStore().setLastFolder(targetFolder);

        loadingBox.setVisible(true);
        scanButton.setDisable(true);
        System.out.println("Initializing scanner for: " + targetFolder);

        Task<Map<String, List<FileData>>> scanTask = new Task<>() {
            @Override
            protected Map<String, List<FileData>> call() throws Exception {
                FileScanner scanner = new FileScanner();
                List<FileData> scannedFiles = scanner.scanDirectory(targetFolder);
                return new DuplicateFinder().findDuplicates(scannedFiles);
            }
        };

        scanTask.setOnSucceeded(e -> {
            Map<String, List<FileData>> duplicates = scanTask.getValue();
            lastScanResults = duplicates;

            // ✅ Save real scanned file count
            int totalFiles = duplicates.values().stream()
                    .mapToInt(List::size).sum();
            new DataStore().updateStats(0, duplicates.size(), totalFiles);

            // ✅ Refresh stat cards immediately
            initialize();

            loadingBox.setVisible(false);
            scanButton.setDisable(false);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/example/pr_1_file_dupe/fxml/results.fxml"));
                Parent resultsScreen = loader.load();

                ResultsController controller = loader.getController();
                controller.displayResults(duplicates);

                BorderPane mainLayout =
                        (BorderPane) pathInputField.getScene().getRoot();
                mainLayout.setCenter(resultsScreen);

            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Could not load results: "
                        + ex.getMessage()).showAndWait();
            }
        });

        scanTask.setOnFailed(e -> {
            loadingBox.setVisible(false);
            scanButton.setDisable(false);
            new Alert(Alert.AlertType.ERROR, "Scan failed: "
                    + scanTask.getException().getMessage()).showAndWait();
        });

        Thread t = new Thread(scanTask);
        t.setDaemon(true);
        t.start();
    }
}