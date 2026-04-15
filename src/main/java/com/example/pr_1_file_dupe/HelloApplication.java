package com.example.pr_1_file_dupe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/com/example/pr_1_file_dupe/fxml/main.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load());

        // ✅ FIXED CSS PATH (IMPORTANT)
        scene.getStylesheets().add(
                getClass().getResource("/com/example/pr_1_file_dupe/CSS/light.css").toExternalForm()
        );

        stage.setTitle("Duplicate File Detector");
        stage.setScene(scene);
        stage.show();
    }
}