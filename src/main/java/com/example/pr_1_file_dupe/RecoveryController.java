package com.example.pr_1_file_dupe;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.awt.Desktop;
import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class RecoveryController {

    @FXML private Label spaceSavedLabel;
    @FXML private Label filesDeletedLabel;
    @FXML private Label scansRunLabel;

    @FXML private TableView<LogEntry> logTable;
    @FXML private TableColumn<LogEntry, String> logTimeCol;
    @FXML private TableColumn<LogEntry, String> logNameCol;
    @FXML private TableColumn<LogEntry, String> logSizeCol;
    @FXML private TableColumn<LogEntry, String> logPathCol;

    // Static session log shared with DuplicatesController
    private static final ObservableList<LogEntry> sessionLog =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Load lifetime stats from DataStore
        DataStore store = new DataStore();
        long savedBytes = Long.parseLong(store.getTotalSaved());

        spaceSavedLabel.setText(formatSize(savedBytes));
        filesDeletedLabel.setText(store.getTotalGroups());
        scansRunLabel.setText(store.getTotalScanned());

        // Wire up the log table columns
        logTimeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().time));
        logNameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));
        logSizeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().size));
        logPathCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().path));

        logTable.setItems(sessionLog);
    }

    /** Called by DuplicatesController after each file is trashed */
    public static void addToLog(FileData file) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        sessionLog.add(new LogEntry(time, file.getName(),
                formatSize(file.getSize()), file.getPath()));
    }

    @FXML
    public void clearLog() {
        sessionLog.clear();
    }

    @FXML
    public void openTrash() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec("explorer.exe shell:RecycleBinFolder");
            } else if (os.contains("mac")) {
                Desktop.getDesktop().open(
                        new File(System.getProperty("user.home") + "/.Trash"));
            } else {
                // Linux — common trash location
                File trash = new File(System.getProperty("user.home")
                        + "/.local/share/Trash/files");
                if (trash.exists()) Desktop.getDesktop().open(trash);
                else showAlert("Could not find Trash folder on this system.");
            }
        } catch (Exception e) {
            showAlert("Unable to open Trash: " + e.getMessage());
        }
    }

    @FXML
    public void resetStats() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Statistics");
        confirm.setHeaderText("Reset all lifetime stats?");
        confirm.setContentText("This will clear your total space saved and deletion history.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                DataStore store = new DataStore();
                store.updateStats(
                        -Long.parseLong(store.getTotalSaved()),
                        -Integer.parseInt(store.getTotalGroups()),
                        -Integer.parseInt(store.getTotalScanned()));
                initialize(); // Refresh labels
            }
        });
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private static String formatSize(long bytes) {
        if (bytes >= 1024L * 1024 * 1024)
            return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
        if (bytes >= 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f KB", bytes / 1024.0);
    }

    /** Simple data class for the session log table */
    public static class LogEntry {
        final String time, name, size, path;
        LogEntry(String time, String name, String size, String path) {
            this.time = time; this.name = name;
            this.size = size; this.path = path;
        }
    }
}
