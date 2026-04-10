package com.example.pr_1_file_dupe;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ResultsController {

    @FXML
    private TreeTableView<FileData> resultsTable;
    @FXML
    private TreeTableColumn<FileData, Boolean> selectColumn;
    @FXML
    private TreeTableColumn<FileData, String> nameColumn;
    @FXML
    private TreeTableColumn<FileData, Long> sizeColumn;
    @FXML
    private TreeTableColumn<FileData, String> pathColumn;

    public void displayResults(Map<String, List<FileData>> duplicates) {

        // Wire up the standard text columns
        nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        sizeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("size"));
        pathColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("path"));

        // Wire up the CheckBox column
        selectColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("selected"));
        selectColumn.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(selectColumn));

        TreeItem<FileData> rootNode = new TreeItem<>(new FileData("Root", "", 0, ""));
        int groupCounter = 1;

        for (Map.Entry<String, List<FileData>> entry : duplicates.entrySet()) {
            List<FileData> fileList = entry.getValue();
            long totalSize = fileList.get(0).getSize();
            long wastedSpace = totalSize * (fileList.size() - 1);

            // Create Group Header
            FileData groupHeaderData = new FileData("Group " + groupCounter + " (" + fileList.size() + " files)", "Group", totalSize, "Wasted: " + (wastedSpace / 1024) + " KB");
            TreeItem<FileData> groupNode = new TreeItem<>(groupHeaderData);
            groupNode.setExpanded(true);

            // Add the actual files
            boolean isFirst = true;
            for (FileData file : fileList) {
                TreeItem<FileData> fileNode = new TreeItem<>(file);

                // Smart Selection: Automatically check the box for everything EXCEPT the very first original file
                if (!isFirst) {
                    file.setSelected(true);
                }
                isFirst = false;

                groupNode.getChildren().add(fileNode);
            }

            rootNode.getChildren().add(groupNode);
            groupCounter++;
        }

        resultsTable.setRoot(rootNode);
        resultsTable.setShowRoot(false);
    }

    @FXML
    public void deleteSelectedFiles() {
        System.out.println("--- DELETION PROCESS STARTED ---");

        TreeItem<FileData> root = resultsTable.getRoot();
        if (root == null) {
            return;
        }

        java.util.List<TreeItem<FileData>> filesToRemove = new java.util.ArrayList<>();
        java.util.List<TreeItem<FileData>> emptyGroupsToRemove = new java.util.ArrayList<>();

        int deletedCount = 0;
        long totalDeletedSizeInBytes = 0; // Initialize size tracker
        java.util.Set<TreeItem<FileData>> affectedGroups = new java.util.HashSet<>(); // Track unique groups

        // 1. Find all checked files
        for (TreeItem<FileData> group : root.getChildren()) {
            for (TreeItem<FileData> fileNode : group.getChildren()) {
                FileData file = fileNode.getValue();

                if (file.isSelected() && !file.getType().equals("Group")) {
                    File target = new File(file.getPath());

                    if (target.exists()) {
                        long fileSize = target.length(); // Get size before deleting
                        
                        boolean success = false;
                        try {
                            // NEW: Move to OS Trash/Recycle Bin instead of permanent deletion!
                            if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.MOVE_TO_TRASH)) {
                                success = java.awt.Desktop.getDesktop().moveToTrash(target);
                            } else {
                                // Fallback to permanent delete if the OS doesn't support Trash
                                success = target.delete(); 
                            }
                        } catch (Exception e) {
                            System.out.println("Error moving to trash: " + e.getMessage());
                        }

                        if (success) {
                            System.out.println("Successfully moved to Trash: " + file.getName());
                            filesToRemove.add(fileNode);

                            // Update our trackers
                            totalDeletedSizeInBytes += fileSize;
                            affectedGroups.add(group);
                            deletedCount++;
                        }
                    }
                            
                    } else {
                        filesToRemove.add(fileNode);
                    }
                }
            }


        // 2. Remove files from UI
        for (TreeItem<FileData> node : filesToRemove) {
            node.getParent().getChildren().remove(node);
        }

        // 3. Remove empty or single-file groups from UI
        for (TreeItem<FileData> group : root.getChildren()) {
            if (group.getChildren().size() <= 1) {
                emptyGroupsToRemove.add(group);
            }
        }
        root.getChildren().removeAll(emptyGroupsToRemove);

        System.out.println("Total files permanently deleted: " + deletedCount);

        // 4. Update Persistence Store
        int deletedGroupsCount = affectedGroups.size();
        com.example.pr_1_file_dupe.DataStore store = new com.example.pr_1_file_dupe.DataStore();
        store.updateStats(totalDeletedSizeInBytes, deletedGroupsCount, 0);

        // Show Alert
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Cleanup Complete");
        alert.setHeaderText(null);
        alert.setContentText("Successfully deleted " + deletedCount + " files and reclaimed " + (totalDeletedSizeInBytes / 1024) + " KB!");
        alert.showAndWait();
    }
    
    }
