package com.example.pr_1_file_dupe;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class FileData {

     private String name;
     private String path;
     private long size;
     private int group_id;

     private BooleanProperty selected = new SimpleBooleanProperty(false);
     
     public FileData(String name, String path, long size,int group_id) {
         this.name = name;
         this.path = path;
         this.size = size;
     }

     public String getName() {
         return name;
     }
     public int getGroup_id() {
         return group_id;
     }


     public String getPath() {
         return path;
     }

     public long getSize() {
         return size;
     }
     public BooleanProperty selectedProperty() {
    	 return selected; 
    	 } 
     public boolean isSelected() { 
    	 return selected.get();
    	 }
     public void setSelected(boolean value) { 
    	 selected.set(value);
    	 }
 }

