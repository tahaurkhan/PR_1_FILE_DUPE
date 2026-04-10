package com.example.pr_1_file_dupe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // 1. Tell it EXACTLY where the file is using an absolute path
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/pr_1_file_dupe/fxml/main.fxml"));
        
        // 2. Load the blank canvas
        Scene scene = new Scene(fxmlLoader.load());

        // 3. Set up the window and show it
        stage.setTitle("Duplicate File Detector");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}