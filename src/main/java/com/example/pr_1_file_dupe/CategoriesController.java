package com.example.pr_1_file_dupe;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;

import java.util.List;
import java.util.Map;

public class CategoriesController {

    @FXML
    private PieChart categoryChart;

    public void generateChart(Map<String, List<FileData>> duplicates) {
        long imageSpace = 0;
        long videoSpace = 0;
        long documentSpace = 0;
        long otherSpace = 0;

        // 1. Sort files and calculate wasted space
        for (List<FileData> fileList : duplicates.values()) {
            if (fileList.size() <= 1) continue;

            long wastedSpace = fileList.get(0).getSize() * (fileList.size() - 1);
            
            // FALLBACK: If it's a completely empty dummy file (0 bytes), pretend it's 1 byte 
            // so it still shows up on our chart for testing purposes!
            if (wastedSpace == 0) wastedSpace = 1; 

            String type = fileList.get(0).getType().toLowerCase();

            switch (type) {
                case "jpg": case "jpeg": case "png": case "gif":
                    imageSpace += wastedSpace;
                    break;
                case "mp4": case "mkv": case "avi": case "mov":
                    videoSpace += wastedSpace;
                    break;
                case "pdf": case "doc": case "docx": case "txt": case "xlsx":
                    documentSpace += wastedSpace;
                    break;
                default:
                    otherSpace += wastedSpace;
                    break;
            }
        }

        // 2. Convert to KB for the labels (so small text files show up as 1.5 KB instead of 0.0 MB)
        double imgKB = imageSpace / 1024.0;
        double vidKB = videoSpace / 1024.0;
        double docKB = documentSpace / 1024.0;
        double othKB = otherSpace / 1024.0;

        // 3. Populate the Pie Chart using the raw byte counts for accurate slice proportions
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        if (imageSpace > 0) pieChartData.add(new PieChart.Data("Images (" + String.format("%.1f", imgKB) + " KB)", imageSpace));
        if (videoSpace > 0) pieChartData.add(new PieChart.Data("Videos (" + String.format("%.1f", vidKB) + " KB)", videoSpace));
        if (documentSpace > 0) pieChartData.add(new PieChart.Data("Documents (" + String.format("%.1f", docKB) + " KB)", documentSpace));
        if (otherSpace > 0) pieChartData.add(new PieChart.Data("Others (" + String.format("%.1f", othKB) + " KB)", otherSpace));

        // 4. Safety Check: If no duplicates were found at all
        if (pieChartData.isEmpty()) {
            categoryChart.setTitle("No duplicates found to chart!");
        } else {
            categoryChart.setTitle("Wasted Space by Category");
        }

        categoryChart.setData(pieChartData);
    }
}