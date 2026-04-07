package com.example.pr_1_file_dupe;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;

public class ResultsController {

    @FXML
    private TableView<FileData> tableView;

    @FXML
    private TableColumn<FileData, String> nameCol;

    @FXML
    private TableColumn<FileData, String> pathCol;

    @FXML
    private TableColumn<FileData, Long> sizeCol;
 
    @FXML
    private TableColumn<FileData, Boolean> selectCol;

    @FXML
    public void initialize() {

        // ✅ Checkbox column
        selectCol.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectCol));

        // ✅ Other columns
        groupCol.setCellValueFactory(new PropertyValueFactory<>("groupId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        pathCol.setCellValueFactory(new PropertyValueFactory<>("path"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));

        // ✅ Real data from scan
        tableView.setItems(DataStore.files);
    }

    // 🔥 DELETE FUNCTION
    @FXML
    private void handleDelete() {

        Iterator<FileData> iterator = DataStore.files.iterator();

        while (iterator.hasNext()) {
            FileData file = iterator.next();

            if (file.isSelected()) {

                File f = new File(file.getPath());

                if (f.exists()) {
                    f.delete(); // 🔥 delete from system
                }

                iterator.remove(); // remove from UI
            }
        }
    }
    

    		@FXML
    		private void handleKeepOne() {

    		    Map<Integer, Boolean> groupSeen = new HashMap<>();

    		    Iterator<FileData> iterator = DataStore.files.iterator();

    		    while (iterator.hasNext()) {
    		        FileData file = iterator.next();

    		        int group = file.getGroup_id();

    		        if (!groupSeen.containsKey(group)) {
    		            // keep first file
    		            groupSeen.put(group, true);
    		        } else {
    		            // delete others
    		            File f = new File(file.getPath());

    		            if (f.exists()) {
    		                f.delete();
    		            }

    		            iterator.remove();
    		        }
    		    }
    		}
    	

}
