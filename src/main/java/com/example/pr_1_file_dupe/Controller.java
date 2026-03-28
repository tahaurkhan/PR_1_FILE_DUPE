package com.example.pr_1_file_dupe;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class Controller {
    @FXML
private BorderPane mainLayout;



    public void Categories(javafx.event.ActionEvent actionEvent) {
        System.out.println("GO to categories");
    }

    public void Recovery(ActionEvent actionEvent) {
        System.out.println("GO to recovery");
    }

    public void Duplicates(ActionEvent actionEvent) {
        System.out.println("GO to duplicates");
    }

    public void Files(ActionEvent actionEvent) {
        System.out.println("GO to files");
    }
}
