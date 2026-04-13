module com.example.pr_1_file_dupe {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.base;

    // GUI controllers
    opens com.example.pr_1_file_dupe to javafx.fxml;
    exports com.example.pr_1_file_dupe;

    // ✅ ADD THESE — service package needs to be accessible
    opens com.example.pr_1_file_dupe.service to javafx.fxml;
    exports com.example.pr_1_file_dupe.service;

    // ✅ ADD THIS — CLI package
    exports com.example.pr_1_file_dupe.service.CLI;
}