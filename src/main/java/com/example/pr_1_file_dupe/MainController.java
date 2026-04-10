package com.example.pr_1_file_dupe;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

public class MainController {

    // This grabs the main shell from our main.fxml
    @FXML
    private BorderPane mainLayout;

    // A helper method that does the heavy lifting of loading screens
    private void loadScreen(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pr_1_file_dupe/fxml/" + fxmlFileName));
            Parent newScreen = loader.load();
            
            // This is the magic line: it replaces whatever is in the center with the new screen!
            mainLayout.setCenter(newScreen);
            
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading screen: " + fxmlFileName);
        }
    }@FXML
    public void openSettings(javafx.event.ActionEvent event) {
        try {
            System.out.println("Opening Settings...");
            // 1. Load the settings screen FXML
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/pr_1_file_dupe/fxml/setting.fxml"));
            javafx.scene.Parent settingsScreen = loader.load();

            // 2. Get the button that was clicked to find the main window
            javafx.scene.control.Button clickedButton = (javafx.scene.control.Button) event.getSource();
            javafx.scene.layout.BorderPane mainLayout = (javafx.scene.layout.BorderPane) clickedButton.getScene().getRoot();

            // 3. Swap the center of the screen to the Settings view
            mainLayout.setCenter(settingsScreen);

        } catch (Exception e) {
            System.out.println("Error loading Settings screen!");
            e.printStackTrace();
        }
    }
    
    // Now our button clicks use the helper method
    @FXML
    public void showFiles(ActionEvent event) {
        // We will tell this to load "dashboard.fxml" when clicked
        loadScreen("dashboard.fxml");
    }

    @FXML
    public void showDuplicates(ActionEvent event) {
        System.out.println("Duplicates screen not built yet!");
    }

    @FXML
    public void showCategories(ActionEvent event) {
    	
        try {
        	System.out.println("Opening Categories...");
            // 1. Load the settings screen FXML
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/pr_1_file_dupe/fxml/categories.fxml"));
            javafx.scene.Parent categoriesScreen = loader.load();

            // 2. Get the button that was clicked to find the main window
            javafx.scene.control.Button clickedButton = (javafx.scene.control.Button) event.getSource();
            javafx.scene.layout.BorderPane mainLayout = (javafx.scene.layout.BorderPane) clickedButton.getScene().getRoot();

            // 3. Swap the center of the screen to the Settings view
            mainLayout.setCenter(categoriesScreen);

        } catch (Exception e) {
            System.out.println("Error loading Caegories screen!");
            e.printStackTrace();
		}
    }

    @FXML
    public void showRecovery(ActionEvent event) {
        System.out.println("Recovery screen not built yet!");
    }
}