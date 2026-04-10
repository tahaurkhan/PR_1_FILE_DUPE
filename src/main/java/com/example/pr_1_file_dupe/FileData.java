package com.example.pr_1_file_dupe;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class FileData {

    private String name;
    private String type;
    private long size;
    private String path;
    
    // NEW: This allows JavaFX to bind a CheckBox directly to this file
    private BooleanProperty selected;

    public FileData(String name, String type, long size, String path) {
        this.name = name;
        this.type = type;
        this.size = size;
        this.path = path;
        this.selected = new SimpleBooleanProperty(false); // Unchecked by default
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public long getSize() { return size; }
    public String getPath() { return path; }

    // NEW: Getter and Setter for the CheckBox
    public BooleanProperty selectedProperty() { return selected; }
    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean isSelected) { this.selected.set(isSelected); }
}