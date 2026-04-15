package com.example.pr_1_file_dupe;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.awt.Desktop;
import java.io.File;

public class RecoveryController {

    @FXML private Label spaceSavedLabel;
    @FXML private Label filesDeletedLabel;
    @FXML private Label scansRunLabel;

    @FXML private TableView<LogEntry> logTable;
    @FXML private TableColumn<LogEntry, String> logTimeCol;
    @FXML private TableColumn<LogEntry, String> logNameCol;
    @FXML private TableColumn<LogEntry, String> logSizeCol;
    @FXML private TableColumn<LogEntry, String> logPathCol;

    // 🔥 Session log (persistent during runtime)
    private static final ObservableList<LogEntry> sessionLog =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        // ✅ Setup table columns
        logTimeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().time));
        logNameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));
        logSizeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().size));
        logPathCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().path));

        logTable.setItems(sessionLog);

        // 🔥 Load stats in background (NO FREEZE)
        Task<DataStore> task = new Task<>() {
            @Override
            protected DataStore call() {
                return new DataStore(); // heavy work
            }
        };

        task.setOnSucceeded(e -> {
            DataStore store = task.getValue();

            long savedBytes = Long.parseLong(store.getTotalSaved());
            spaceSavedLabel.setText(formatSize(savedBytes));
            filesDeletedLabel.setText(store.getTotalGroups());
            scansRunLabel.setText(store.getTotalScanned());
        });

        task.setOnFailed(e -> {
            System.out.println("Error loading stats: " + task.getException());
        });

        new Thread(task).start();
    }

    // 🔥 Add log entry (called from DuplicatesController)
    public static void addToLog(FileData file) {
        String time = java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

        sessionLog.add(0, new LogEntry(
                time,
                file.getName(),
                formatSize(file.getSize()),
                file.getPath()
        ));
    }

    // 🧹 Clear log
    @FXML
    public void clearLog() {
        sessionLog.clear();
    }

    // 🗑 Open system trash (non-blocking)
    @FXML
    public void openTrash() {
        new Thread(() -> {
            try {
                String os = System.getProperty("os.name").toLowerCase();

                if (os.contains("win")) {
                    Runtime.getRuntime().exec("explorer.exe shell:RecycleBinFolder");

                } else if (os.contains("mac")) {
                    Desktop.getDesktop().open(
                            new File(System.getProperty("user.home") + "/.Trash"));

                } else {
                    File trash = new File(System.getProperty("user.home")
                            + "/.local/share/Trash/files");

                    if (trash.exists()) {
                        Desktop.getDesktop().open(trash);
                    } else {
                        Platform.runLater(() ->
                                showAlert("Trash folder not found."));
                    }
                }

            } catch (Exception e) {
                Platform.runLater(() ->
                        showAlert("Unable to open Trash: " + e.getMessage()));
            }
        }).start();
    }

    // 🔄 Reset stats safely
    @FXML
    public void resetStats() {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Statistics");
        confirm.setHeaderText("Reset all lifetime stats?");
        confirm.setContentText("This will clear your total data.");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {

                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        DataStore store = new DataStore();

                        store.updateStats(
                                -Long.parseLong(store.getTotalSaved()),
                                -Integer.parseInt(store.getTotalGroups()),
                                -Integer.parseInt(store.getTotalScanned())
                        );

                        return null;
                    }
                };

                task.setOnSucceeded(e -> {
                    spaceSavedLabel.setText("0 KB");
                    filesDeletedLabel.setText("0");
                    scansRunLabel.setText("0");
                });

                new Thread(task).start();
            }
        });
    }

    // ⚠️ Alert helper
    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // 📦 Format file size
    private static String formatSize(long bytes) {
        if (bytes >= 1024L * 1024 * 1024)
            return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
        if (bytes >= 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f KB", bytes / 1024.0);
    }

    // 📄 Log entry model
    public static class LogEntry {
        final String time, name, size, path;

        LogEntry(String time, String name, String size, String path) {
            this.time = time;
            this.name = name;
            this.size = size;
            this.path = path;
        }
    }
}