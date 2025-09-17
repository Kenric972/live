module org.example.videoscrapper {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires org.json;
    requires javafx.web;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;
    requires com.google.gson;
    requires java.sql;

    // Open your org.example.videoscrapper.model package to Jackson for reflection

    // Keep exports if other modules need compile-time access
    exports org.example.videoscrapper.model;
    exports org.example.videoscrapper;
    exports org.example.videoscrapper.Controllers;

    // Open your packages to JavaFX for FXML reflection
    opens org.example.videoscrapper to javafx.fxml;
    opens org.example.videoscrapper.Controllers to javafx.fxml;
    exports org.example.videoscrapper.services;
    opens org.example.videoscrapper.services to javafx.fxml;
    opens org.example.videoscrapper.model to com.fasterxml.jackson.databind, javafx.fxml;

}
