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
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #a1c4fd, #c2e9fb);");

        VBox content = new VBox(10);
        content.setStyle("-fx-alignment: center;");

        // =========================
        // 🔥 LOAD HIGH-QUALITY LOGO (PNG ONLY)
        // =========================
        Image logoImage = null;

        try {
            URL logoUrl = getClass().getResource("/com/example/pr_1_file_dupe/img/logo.png");

            if (logoUrl != null) {
                logoImage = new Image(logoUrl.toExternalForm());

                ImageView logoView = new ImageView(logoImage);
                logoView.setFitWidth(120);
                logoView.setPreserveRatio(true);

                // Smooth rendering
                logoView.setSmooth(true);
                logoView.setCache(true);

                content.getChildren().add(logoView);
            } else {
                System.out.println("Logo not found!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // =========================
        // 🏷️ TEXT
        // =========================
        Label title = new Label("Duplicate File Detector");
        title.setStyle("-fx-text-fill: #3f5145; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label subtitle = new Label("Smart File Cleaner");
        subtitle.setStyle("-fx-text-fill: #594545; -fx-font-size: 14px;");

        content.getChildren().addAll(title, subtitle);
        root.getChildren().add(content);

        Scene splashScene = new Scene(root, 500, 300);

        // Transparent splash
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
        Image finalLogoImage = logoImage; // keep reference

        PauseTransition delay = new PauseTransition(Duration.seconds(2.5));
        delay.setOnFinished(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/pr_1_file_dupe/fxml/main.fxml")
                );

                Scene mainScene = new Scene(loader.load(), 920, 650);

                // Apply theme
                ThemeManager.apply(mainScene);

                // Set title
                primaryStage.setTitle("Duplicate File Detector");

                // ✅ SET APP ICON (TASKBAR + WINDOW)
                if (finalLogoImage != null) {
                    primaryStage.getIcons().add(finalLogoImage);
                }

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