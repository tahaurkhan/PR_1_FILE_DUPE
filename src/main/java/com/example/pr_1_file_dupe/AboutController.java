package com.example.pr_1_file_dupe;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class AboutController {

    // Developer 1 Social Media URLs (Replace with actual URLs)
    private static final String DEV1_LINKEDIN = "https://www.linkedin.com/in/tahaur-khan";
    private static final String DEV1_GITHUB = "https://github.com/tahaurkhan";
    private static final String DEV1_TWITTER = "https://twitter.com/tahaur";

    // Developer 2 Social Media URLs (Replace with actual URLs)
    private static final String DEV2_LINKEDIN = "https://www.linkedin.com/in/praveen-gupta";
    private static final String DEV2_GITHUB = "https://github.com/guptapraveen67984-dev";
    private static final String DEV2_TWITTER = "https://twitter.com/praveengupta";

    // ═══════════════════════════════════════════════
    //  DEVELOPER 1 SOCIAL MEDIA
    // ═══════════════════════════════════════════════
    @FXML
    public void openDev1LinkedIn() {
        openURL(DEV1_LINKEDIN);
    }

    @FXML
    public void openDev1GitHub() {
        openURL(DEV1_GITHUB);
    }

    @FXML
    public void openDev1Twitter() {
        openURL(DEV1_TWITTER);
    }

    // ═══════════════════════════════════════════════
    //  DEVELOPER 2 SOCIAL MEDIA
    // ═══════════════════════════════════════════════
    @FXML
    public void openDev2LinkedIn() {
        openURL(DEV2_LINKEDIN);
    }

    @FXML
    public void openDev2GitHub() {
        openURL(DEV2_GITHUB);
    }

    @FXML
    public void openDev2Twitter() {
        openURL(DEV2_TWITTER);
    }

    // ═══════════════════════════════════════════════
    //  HELPER METHOD
    // ═══════════════════════════════════════════════
    private void openURL(String url) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    desktop.browse(new java.net.URI(url));
                } else {
                    showError("Browser not supported on this system.");
                }
            } else {
                showError("Desktop not supported on this system.");
            }
        } catch (Exception e) {
            showError("Could not open URL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}