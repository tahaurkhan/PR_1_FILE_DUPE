package com.example.pr_1_file_dupe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

    	
    	FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/pr_1_file_dupe/fxml/main.fxml"));

        Scene scene = new Scene(fxmlLoader.load());
        String css;
        css = this.getClass().getResource("/com/example/pr_1_file_dupe/CSS/application.css").toExternalForm();

        if (css == null) {
            throw new RuntimeException("CSS file not found!");
        }
        scene.getStylesheets().add(css);

        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}