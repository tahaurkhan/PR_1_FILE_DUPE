package com.example.pr_1_file_dupe;

import com.example.pr_1_file_dupe.service.FileScanner;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class DashboardController {

    @FXML private TextField pathInputField;
    @FXML private Button scanButton;
    @FXML private VBox loadingBox;
    @FXML private Label totalSavedLabel;
    @FXML private Label groupsFoundLabel;
    @FXML private Label scannedCountLabel;
    // Connects to our new loading UI
 // NEW: A temporary storage for our last scan so other screens can see it
    
    
    
    public static java.util.Map<String, java.util.List<com.example.pr_1_file_dupe.FileData>> lastScanResults;
    @FXML
    public void initialize() {
        DataStore store = new DataStore();
        
        // Convert bytes to a readable format (KB/MB)
        long savedBytes = Long.parseLong(store.getTotalSaved());
        String formattedSize = (savedBytes > 1024 * 1024) 
            ? (savedBytes / (1024 * 1024)) + " MB" 
            : (savedBytes / 1024) + " KB";

        totalSavedLabel.setText(formattedSize);
        groupsFoundLabel.setText(store.getTotalGroups());
        scannedCountLabel.setText(store.getTotalScanned());
    }
 // NEW: Method to open the OS File Browser
    @FXML
    public void browseFolder() {
        // 1. Create the JavaFX Directory Chooser
        javafx.stage.DirectoryChooser directoryChooser = new javafx.stage.DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Scan");
        
        // Optional: Set default starting location to user's home directory
        directoryChooser.setInitialDirectory(new java.io.File(System.getProperty("user.home")));

        // 2. Open the window and wait for the user to select something
        javafx.stage.Window stage = pathInputField.getScene().getWindow();
        java.io.File selectedDirectory = directoryChooser.showDialog(stage);

        // 3. If they picked a folder (and didn't click cancel), put the path in the text box
        if (selectedDirectory != null) {
            pathInputField.setText(selectedDirectory.getAbsolutePath());
        }
    }
    @FXML
    public void startScan(ActionEvent event) {
        String targetFolder = pathInputField.getText();

        if (targetFolder == null || targetFolder.trim().isEmpty()) {
            System.out.println("Please enter a valid path!");
            return;
        }
        
        // 1. Show the loading animation and disable the button so the user doesn't click it twice
        loadingBox.setVisible(true);
        scanButton.setDisable(true);
        System.out.println("Initializing scanner for: " + targetFolder);

        // 2. Create the Background Task
        Task<Map<String, List<FileData>>> scanTask = new Task<>() {
            @Override
            protected Map<String, List<FileData>> call() throws Exception {
                // Everything in here runs on a separate invisible thread!
                FileScanner scanner = new FileScanner();
                List<FileData> scannedFiles = scanner.scanDirectory(targetFolder);

                DuplicateFinder finder = new DuplicateFinder();
                return finder.findDuplicates(scannedFiles);
            }
        };
      
        // 3. Define what happens when the background task successfully finishes
     // 3. Define what happens when the background task successfully finishes
        scanTask.setOnSucceeded(e -> {
            Map<String, List<FileData>> duplicates = scanTask.getValue();
            
            // ---> NEW: Get the scanner instance out of the task if possible, 
            // or just print the map sizes to verify it worked.
            System.out.println("Scan complete! Handing data to Results Screen...");
            
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/pr_1_file_dupe/fxml/results.fxml"));
                javafx.scene.Parent resultsScreen = loader.load();

                ResultsController controller = loader.getController();
                controller.displayResults(duplicates);

                // Swap the screen
                javafx.scene.layout.BorderPane mainLayout = (javafx.scene.layout.BorderPane) pathInputField.getScene().getRoot();
                mainLayout.setCenter(resultsScreen);

            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Error loading results screen!");
            }
        });

        // 4. Start the Background Thread
        Thread backgroundThread = new Thread(scanTask);
        backgroundThread.setDaemon(true); // Ensures the background thread dies gracefully if the user closes the app
        backgroundThread.start();
    }
}