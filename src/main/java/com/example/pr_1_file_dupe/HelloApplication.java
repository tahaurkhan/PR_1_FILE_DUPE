package com.example.pr_1_file_dupe;

import javafx.animation.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) {

        // =========================
        // 🎬 SPLASH UI
        // =========================
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #0f2027, #203a43, #2c5364);");

        VBox content = new VBox(10);
        content.setStyle("-fx-alignment: center;");

        // 🔥 ADD APP LOGO
        try {
            URL logoUrl = getClass().getResource("/com/example/pr_1_file_dupe/images/logo.png");
            if (logoUrl != null) {
                Image logoImage = new Image(logoUrl.toExternalForm());
                ImageView logoView = new ImageView(logoImage);
                logoView.setFitWidth(100); // Size of logo
                logoView.setPreserveRatio(true);
                content.getChildren().add(logoView);
                
                // Add logo to the main app window taskbar
                primaryStage.getIcons().add(logoImage);
            }
        } catch (Exception e) {
            System.out.println("No logo.png found in images folder.");
        }

        Label title = new Label("Duplicate File Detector");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label subtitle = new Label("Smart File Cleaner");
        subtitle.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14px;");

        content.getChildren().addAll(title, subtitle);

        root.getChildren().add(content);

        Scene splashScene = new Scene(root, 500, 300);
        
        // Transparent stage for modern look
        Stage splashStage = new Stage(StageStyle.TRANSPARENT);
        splashScene.setFill(null);
        splashStage.setScene(splashScene);
        splashStage.show();

        // =========================
        // 🎥 ANIMATIONS
        // =========================

        FadeTransition fade = new FadeTransition(Duration.seconds(1.5), content);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(1.5), content);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1);
        scale.setToY(1);

        TranslateTransition floating = new TranslateTransition(Duration.seconds(2), content);
        floating.setFromY(10);
        floating.setToY(-10);
        floating.setAutoReverse(true);
        floating.setCycleCount(Animation.INDEFINITE);

        ParallelTransition intro = new ParallelTransition(fade, scale);
        intro.play();

        intro.setOnFinished(e -> floating.play());

        // =========================
        // ⏳ LOAD MAIN APP
        // =========================
        PauseTransition delay = new PauseTransition(Duration.seconds(2.5));
        delay.setOnFinished(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/pr_1_file_dupe/fxml/main.fxml")
                );

                Scene mainScene = new Scene(loader.load(), 920, 650);
                ThemeManager.apply(mainScene);
                primaryStage.setTitle("Duplicate File Detector");
                primaryStage.setScene(mainScene);
                primaryStage.show();

                splashStage.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        delay.play();
    }

    public static void main(String[] args) {
        launch();
    }
}