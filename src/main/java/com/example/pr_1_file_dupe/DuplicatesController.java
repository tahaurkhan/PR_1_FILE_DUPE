package com.example.pr_1_file_dupe;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

import java.io.File;
import java.util.*;

public class DuplicatesController {

    @FXML private TreeTableView<FileData> duplicatesTable;
    @FXML private TreeTableColumn<FileData, Boolean> selectCol;
    @FXML private TreeTableColumn<FileData, String>  nameCol;
    @FXML private TreeTableColumn<FileData, String>  typeCol;
    @FXML private TreeTableColumn<FileData, Long>    sizeCol;
    @FXML private TreeTableColumn<FileData, String>  pathCol;

    @FXML private Label totalGroupsLabel;
    @FXML private Label wastedSpaceLabel;
    @FXML private Label totalFilesLabel;
    @FXML private Label selectedCountLabel;
    @FXML private Label noDataPane;

    @FXML private ToggleButton filterAll;
    @FXML private ToggleButton filterImages;
    @FXML private ToggleButton filterVideos;
    @FXML private ToggleButton filterDocs;
    @FXML private ToggleButton filterOthers;
    @FXML private ComboBox<String> sortDropdown;

    // Keep master copy for filtering
    private Map<String, List<FileData>> masterData;

    private static final Set<String> IMAGE_TYPES =
            Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp");
    private static final Set<String> VIDEO_TYPES =
            Set.of("mp4", "mkv", "avi", "mov", "wmv");
    private static final Set<String> DOC_TYPES =
            Set.of("pdf", "doc", "docx", "txt", "xlsx", "pptx", "csv");

    @FXML
    public void initialize() {
        // Wire up columns
        selectCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("selected"));
        selectCol.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(selectCol));
        nameCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        typeCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("type"));
        sizeCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("size"));
        pathCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("path"));

        sortDropdown.setValue("Largest First");

        // Load last scan results if available
        if (DashboardController.lastScanResults != null
                && !DashboardController.lastScanResults.isEmpty()) {
            masterData = DashboardController.lastScanResults;
            buildTable(masterData);
        } else {
            showNoData(true);
        }
    }

    private void buildTable(Map<String, List<FileData>> data) {
        showNoData(data == null || data.isEmpty());
        if (data == null || data.isEmpty()) return;

        TreeItem<FileData> root = new TreeItem<>(
                new FileData("Root", "", 0, ""));

        long totalWasted = 0;
        int totalFiles  = 0;
        int groupNum    = 1;

        // Sort the entries
        List<Map.Entry<String, List<FileData>>> entries = new ArrayList<>(data.entrySet());
        sortEntries(entries);

        for (Map.Entry<String, List<FileData>> entry : entries) {
            List<FileData> files = entry.getValue();
            long wastedBytes = files.get(0).getSize() * (files.size() - 1);
            totalWasted += wastedBytes;
            totalFiles  += files.size();

            String wastedStr = formatSize(wastedBytes);
            FileData header = new FileData(
                    "Group " + groupNum + "  (" + files.size() + " files)",
                    "Group", files.get(0).getSize(),
                    "Wasted: " + wastedStr);

            TreeItem<FileData> groupNode = new TreeItem<>(header);
            groupNode.setExpanded(true);

            boolean isFirst = true;
            for (FileData f : files) {
                f.setSelected(!isFirst);   // Auto-check all but first copy
                groupNode.getChildren().add(new TreeItem<>(f));
                isFirst = false;
            }

            root.getChildren().add(groupNode);
            groupNum++;
        }

        duplicatesTable.setRoot(root);
        duplicatesTable.setShowRoot(false);

        totalGroupsLabel.setText(String.valueOf(data.size()));
        wastedSpaceLabel.setText(formatSize(totalWasted));
        totalFilesLabel.setText(String.valueOf(totalFiles));
        refreshSelectedCount();
    }

    @FXML
    public void applyFilter() {
        if (masterData == null) return;

        if (filterAll.isSelected()) {
            buildTable(masterData);
            return;
        }

        Map<String, List<FileData>> filtered = new LinkedHashMap<>();
        for (Map.Entry<String, List<FileData>> entry : masterData.entrySet()) {
            String type = entry.getValue().get(0).getType().toLowerCase();
            boolean keep = false;

            if (filterImages.isSelected() && IMAGE_TYPES.contains(type)) keep = true;
            if (filterVideos.isSelected() && VIDEO_TYPES.contains(type)) keep = true;
            if (filterDocs.isSelected()   && DOC_TYPES.contains(type))   keep = true;
            if (filterOthers.isSelected()
                    && !IMAGE_TYPES.contains(type)
                    && !VIDEO_TYPES.contains(type)
                    && !DOC_TYPES.contains(type)) keep = true;

            if (keep) filtered.put(entry.getKey(), entry.getValue());
        }
        buildTable(filtered);
    }

    @FXML
    public void applySort() {
        if (masterData != null) buildTable(masterData);
    }

    private void sortEntries(List<Map.Entry<String, List<FileData>>> entries) {
        String sort = sortDropdown.getValue();
        if (sort == null) return;
        switch (sort) {
            case "Largest First"  ->
                    entries.sort((a, b) -> Long.compare(b.getValue().get(0).getSize(),
                            a.getValue().get(0).getSize()));
            case "Smallest First" ->
                    entries.sort(Comparator.comparingLong(e -> e.getValue().get(0).getSize()));
            case "Most Copies"    ->
                    entries.sort((a, b) -> Integer.compare(b.getValue().size(),
                            a.getValue().size()));
            case "File Name A-Z"  ->
                    entries.sort(Comparator.comparing(e -> e.getValue().get(0).getName()));
        }
    }

    @FXML
    public void selectAll() {
        forEachFile(f -> f.setSelected(true));
        refreshSelectedCount();
    }

    @FXML
    public void selectNone() {
        forEachFile(f -> f.setSelected(false));
        refreshSelectedCount();
    }

    @FXML
    public void selectAllButFirst() {
        TreeItem<FileData> root = duplicatesTable.getRoot();
        if (root == null) return;
        for (TreeItem<FileData> group : root.getChildren()) {
            boolean first = true;
            for (TreeItem<FileData> node : group.getChildren()) {
                node.getValue().setSelected(!first);
                first = false;
            }
        }
        refreshSelectedCount();
    }

    @FXML
    public void deleteSelected() {
        TreeItem<FileData> root = duplicatesTable.getRoot();
        if (root == null) return;

        List<TreeItem<FileData>> toRemove   = new ArrayList<>();
        List<TreeItem<FileData>> emptyGroups = new ArrayList<>();
        int    deleted   = 0;
        long   totalSize = 0;

        for (TreeItem<FileData> group : root.getChildren()) {
            for (TreeItem<FileData> node : group.getChildren()) {
                FileData f = node.getValue();
                if (f.isSelected() && !f.getType().equals("Group")) {
                    File target = new File(f.getPath());
                    boolean success = false;
                    if (target.exists()) {
                        try {
                            if (java.awt.Desktop.isDesktopSupported()
                                    && java.awt.Desktop.getDesktop()
                                    .isSupported(java.awt.Desktop.Action.MOVE_TO_TRASH)) {
                                success = java.awt.Desktop.getDesktop().moveToTrash(target);
                            } else {
                                success = target.delete();
                            }
                        } catch (Exception e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                        if (success) {
                            totalSize += f.getSize();
                            deleted++;
                            // Log to RecoveryController session log
                            RecoveryController.addToLog(f);
                        }
                    }
                    toRemove.add(node);
                }
            }
        }

        toRemove.forEach(n -> n.getParent().getChildren().remove(n));
        root.getChildren().stream()
                .filter(g -> g.getChildren().size() <= 1)
                .forEach(emptyGroups::add);
        root.getChildren().removeAll(emptyGroups);

        new DataStore().updateStats(totalSize, emptyGroups.size(), 0);
        refreshSelectedCount();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Done");
        alert.setHeaderText(null);
        alert.setContentText("Moved " + deleted + " files to Trash ("
                + formatSize(totalSize) + " reclaimed).");
        alert.showAndWait();
    }

    private void forEachFile(java.util.function.Consumer<FileData> action) {
        TreeItem<FileData> root = duplicatesTable.getRoot();
        if (root == null) return;
        for (TreeItem<FileData> group : root.getChildren())
            for (TreeItem<FileData> node : group.getChildren())
                action.accept(node.getValue());
    }

    private void refreshSelectedCount() {
        long count = 0;
        TreeItem<FileData> root = duplicatesTable.getRoot();
        if (root != null)
            for (TreeItem<FileData> g : root.getChildren())
                for (TreeItem<FileData> n : g.getChildren())
                    if (n.getValue().isSelected()) count++;
        selectedCountLabel.setText(count + " files selected");
    }

    private void showNoData(boolean show) {
        noDataPane.setVisible(show);
        noDataPane.setManaged(show);
        duplicatesTable.setVisible(!show);
        duplicatesTable.setManaged(!show);
    }

    private String formatSize(long bytes) {
        if (bytes >= 1024L * 1024 * 1024)
            return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
        if (bytes >= 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f KB", bytes / 1024.0);
    }
}