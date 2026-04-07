
package com.example.pr_1_file_dupe;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

public class DataStore {

    // Shared file list across screens
    public static ObservableList<FileData> files = FXCollections.observableArrayList();

}

