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

            // Wasted space = size of file * (number of copies - 1)
            long wastedSpace = fileList.get(0).getSize() * (fileList.size() - 1);
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

        // 2. Convert bytes to Megabytes (MB) for cleaner chart numbers
        double imgMB = imageSpace / (1024.0 * 1024.0);
        double vidMB = videoSpace / (1024.0 * 1024.0);
        double docMB = documentSpace / (1024.0 * 1024.0);
        double othMB = otherSpace / (1024.0 * 1024.0);

        // 3. Populate the Pie Chart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        if (imgMB > 0) pieChartData.add(new PieChart.Data("Images (" + String.format("%.1f", imgMB) + " MB)", imgMB));
        if (vidMB > 0) pieChartData.add(new PieChart.Data("Videos (" + String.format("%.1f", vidMB) + " MB)", vidMB));
        if (docMB > 0) pieChartData.add(new PieChart.Data("Documents (" + String.format("%.1f", docMB) + " MB)", docMB));
        if (othMB > 0) pieChartData.add(new PieChart.Data("Others (" + String.format("%.1f", othMB) + " MB)", othMB));

        categoryChart.setData(pieChartData);
    }
}