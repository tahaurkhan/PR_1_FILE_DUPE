module com.example.pr_1_file_dupe {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.media; // 🔥 ADD THIS for sound support

    // GUI controllers
    opens com.example.pr_1_file_dupe to javafx.fxml;
    exports com.example.pr_1_file_dupe;

    // Service package
    opens com.example.pr_1_file_dupe.service to javafx.fxml;
    exports com.example.pr_1_file_dupe.service;

    // CLI package
    exports com.example.pr_1_file_dupe.service.CLI;
    
    // 🔥 ADD THIS: Utils package for SoundManager
    opens com.example.pr_1_file_dupe.utils to javafx.fxml;
    exports com.example.pr_1_file_dupe.utils;
}