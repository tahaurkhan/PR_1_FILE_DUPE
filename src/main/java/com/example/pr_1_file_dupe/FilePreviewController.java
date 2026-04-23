package com.example.pr_1_file_dupe;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;

public class FilePreviewController {

    @FXML private BorderPane previewRoot;
    @FXML private Label fileNameLabel;
    @FXML private Label filePathLabel;
    @FXML private Label fileSizeLabel;
    @FXML private Label fileTypeLabel;
    @FXML private VBox previewContainer;
    @FXML private ImageView imagePreview;
    @FXML private Label textPreview;

    private FileData fileData;

    public void setFileData(FileData data) {
        this.fileData = data;
        loadPreview();
    }

    private void loadPreview() {
        if (fileData == null) return;

        // Set file info
        fileNameLabel.setText(fileData.getName());
        filePathLabel.setText(fileData.getPath());
        fileSizeLabel.setText(formatSize(fileData.getSize()));
        fileTypeLabel.setText(fileData.getType().toUpperCase());

        // Load preview based on file type
        String extension = fileData.getType().toLowerCase();
        File file = new File(fileData.getPath());

        try {
            if (isImageFile(extension)) {
                // Show image preview
                Image image = new Image(file.toURI().toString());
                imagePreview.setImage(image);
                imagePreview.setVisible(true);
                imagePreview.setManaged(true);
                textPreview.setVisible(false);
                textPreview.setManaged(false);
                
            } else if (isTextFile(extension)) {
                
                // 🔥 FIXED: Smart Size Limiting (2 MB max) instead of character truncation
                long maxSizeBytes = 2 * 1024 * 1024; 
                
                if (file.length() > maxSizeBytes) {
                    textPreview.setText("File is too large to preview safely (> 2MB).\n\nPlease close this window and use the gray '↗ Open' button to view it in your system's default editor.");
                } else {
                    // File is safe size, load the FULL document
                    String content = Files.readString(file.toPath());
                    textPreview.setText(content);
                }
                
                textPreview.setVisible(true);
                textPreview.setManaged(true);
                imagePreview.setVisible(false);
                imagePreview.setManaged(false);
                
            } else {
                // Show file info only
                textPreview.setText("Preview not available for this file type.");
                textPreview.setVisible(true);
                textPreview.setManaged(true);
                imagePreview.setVisible(false);
                imagePreview.setManaged(false);
            }
        } catch (Exception e) {
            textPreview.setText("Error loading preview: " + e.getMessage());
            textPreview.setVisible(true);
            textPreview.setManaged(true);
            imagePreview.setVisible(false);
            imagePreview.setManaged(false);
        }
    }

    @FXML
    public void closePreview() {
        Stage stage = (Stage) previewRoot.getScene().getWindow();
        stage.close();
    }

    private boolean isImageFile(String ext) {
        return ext.matches("jpg|jpeg|png|gif|bmp|webp");
    }

    private boolean isTextFile(String ext) {
        return ext.matches("txt|java|xml|json|csv|log|md|properties");
    }

    private String formatSize(long bytes) {
        if (bytes >= 1024L * 1024 * 1024)
            return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
        if (bytes >= 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f KB", bytes / 1024.0);
    }
}