module com.example.pr_1_file_dupe {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.example.pr_1_file_dupe to javafx.fxml;
    exports com.example.pr_1_file_dupe;
}