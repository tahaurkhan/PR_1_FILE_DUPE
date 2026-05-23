package com.example.pr_1_file_dupe;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.util.List;
import java.util.Map;

public class CategoriesController {

    @FXML private Label totalWastedLabel;
    @FXML private Label totalFilesLabel;

    @FXML private Label imagesInfoLabel;
    @FXML private Label imagesCountLabel;
    @FXML private ProgressBar imagesProgressBar;

    @FXML private Label videosInfoLabel;
    @FXML private Label videosCountLabel;
    @FXML private ProgressBar videosProgressBar;

    @FXML private Label docsInfoLabel;
    @FXML private Label docsCountLabel;
    @FXML private ProgressBar docsProgressBar;

    @FXML private Label othersInfoLabel;
    @FXML private Label othersCountLabel;
    @FXML private ProgressBar othersProgressBar;

    public void generateChart(Map<String, List<FileData>> duplicates) {
        long imageSpace = 0, videoSpace = 0, documentSpace = 0, otherSpace = 0;
        int imageCount = 0, videoCount = 0, documentCount = 0, otherCount = 0;
        
        long totalWastedSpace = 0;
        int totalWastedFiles = 0;

        if (duplicates != null) {
            for (List<FileData> fileList : duplicates.values()) {
                if (fileList.size() <= 1) continue;

                int redundantCount = fileList.size() - 1;
                long wastedSpace = fileList.get(0).getSize() * redundantCount;
                
                totalWastedSpace += wastedSpace;
                totalWastedFiles += redundantCount;

                String type = fileList.get(0).getType().toLowerCase();

                switch (type) {
                    case "jpg": case "jpeg": case "png": case "gif": case "bmp": case "webp":
                        imageSpace += wastedSpace;
                        imageCount += redundantCount;
                        break;
                    case "mp4": case "mkv": case "avi": case "mov": case "wmv": case "flv":
                        videoSpace += wastedSpace;
                        videoCount += redundantCount;
                        break;
                    case "pdf": case "doc": case "docx": case "txt": case "xlsx": case "xls": case "csv": case "ppt": case "pptx":
                        documentSpace += wastedSpace;
                        documentCount += redundantCount;
                        break;
                    default:
                        otherSpace += wastedSpace;
                        otherCount += redundantCount;
                        break;
                }
            }
        }

        // Set Grand Totals
        totalWastedLabel.setText(formatSize(totalWastedSpace));
        totalFilesLabel.setText(totalWastedFiles + " redundant duplicate copies found");

        // Calculate Percentages and Set Progress Bars
        double imgPercent = totalWastedSpace > 0 ? (double) imageSpace / totalWastedSpace : 0;
        double vidPercent = totalWastedSpace > 0 ? (double) videoSpace / totalWastedSpace : 0;
        double docPercent = totalWastedSpace > 0 ? (double) documentSpace / totalWastedSpace : 0;
        double othPercent = totalWastedSpace > 0 ? (double) otherSpace / totalWastedSpace : 0;

        // Set Info Labels
        imagesInfoLabel.setText(formatSize(imageSpace) + " (" + String.format("%.1f", imgPercent * 100) + "%)");
        videosInfoLabel.setText(formatSize(videoSpace) + " (" + String.format("%.1f", vidPercent * 100) + "%)");
        docsInfoLabel.setText(formatSize(documentSpace) + " (" + String.format("%.1f", docPercent * 100) + "%)");
        othersInfoLabel.setText(formatSize(otherSpace) + " (" + String.format("%.1f", othPercent * 100) + "%)");

        // Set Count Labels
        imagesCountLabel.setText(imageCount + " duplicate copies can be removed");
        videosCountLabel.setText(videoCount + " duplicate copies can be removed");
        docsCountLabel.setText(documentCount + " duplicate copies can be removed");
        othersCountLabel.setText(otherCount + " duplicate copies can be removed");

        // Set Progress Bars
        imagesProgressBar.setProgress(imgPercent);
        videosProgressBar.setProgress(vidPercent);
        docsProgressBar.setProgress(docPercent);
        othersProgressBar.setProgress(othPercent);
    }

    private String formatSize(long bytes) {
        if (bytes >= 1024L * 1024 * 1024)
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        if (bytes >= 1024 * 1024)
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        if (bytes >= 1024)
            return String.format("%.2f KB", bytes / 1024.0);
        return bytes + " Bytes";
    }
}