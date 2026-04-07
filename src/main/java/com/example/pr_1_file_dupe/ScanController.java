//```java id="scancontroller01"
package com.example.pr_1_file_dupe;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class ScanController {

    @FXML
    private TextField pathField;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label filesLabel, currentFileLabel, speedLabel, skipCountLabel;

    private int filesScanned = 0;
    private int skipped = 0;

    // 📁 Browse Button
    @FXML
    private void handleBrowse() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Folder");

        File selectedDir = chooser.showDialog(new Stage());

        if (selectedDir != null) {
            pathField.setText(selectedDir.getAbsolutePath());
        }
    }

    // ▶ Start Scan (SIMULATION)
    @FXML
    private void handleStart() {

        filesScanned = 0;
        skipped = 0;
        progressBar.setProgress(0);

        new Thread(() -> {
            for (int i = 1; i <= 100; i++) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int current = i;

                javafx.application.Platform.runLater(() -> {
                    progressBar.setProgress(current / 100.0);
                    filesLabel.setText("Files scanned: " + current);
                    currentFileLabel.setText("Current file: file_" + current + ".txt");
                    speedLabel.setText("Speed: " + (10 + current % 5) + " files/sec");
                });
            }
        }).start();
    }

    // ⏸ Pause (basic)
    @FXML
    private void handlePause() {
        System.out.println("Pause clicked (implement later)");
    }

    // ❌ Cancel
    @FXML
    private void handleCancel() {
        progressBar.setProgress(0);
        filesLabel.setText("Files scanned: 0");
        currentFileLabel.setText("Current file: -");
    }

    // ⏭ Skip File
    @FXML
    private void handleSkip() {
        skipped++;
        skipCountLabel.setText("Skipped files: " + skipped);
    }
}

