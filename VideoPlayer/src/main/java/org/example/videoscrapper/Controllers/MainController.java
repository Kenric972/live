package org.example.videoscrapper.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class MainController {

    @FXML
    private AnchorPane contentArea;

    @FXML
    public void initialize() throws IOException {
        showHome();
    }

    @FXML
    private void showHome() throws IOException {
        AnchorPane pane = FXMLLoader.load(getClass().getResource("/org/example/videoscrapper/HomePage.fxml"));
        contentArea.getChildren().setAll(pane);
        AnchorPane.setTopAnchor(pane, 0.0);
        AnchorPane.setBottomAnchor(pane, 0.0);
        AnchorPane.setLeftAnchor(pane, 0.0);
        AnchorPane.setRightAnchor(pane, 0.0);
    }

    @FXML
    private void showDownloads() throws IOException {
        AnchorPane pane = FXMLLoader.load(getClass().getResource("/org/example/videoscrapper/Library.fxml"));
        contentArea.getChildren().setAll(pane);
        AnchorPane.setTopAnchor(pane, 0.0);
        AnchorPane.setBottomAnchor(pane, 0.0);
        AnchorPane.setLeftAnchor(pane, 0.0);
        AnchorPane.setRightAnchor(pane, 0.0);
    }

    @FXML
    private void showVideoPlayer() throws IOException {
        AnchorPane pane = FXMLLoader.load(getClass().getResource("/org/example/videoscrapper/UrlStreaming.fxml"));
        contentArea.getChildren().setAll(pane);
        AnchorPane.setTopAnchor(pane, 0.0);
        AnchorPane.setBottomAnchor(pane, 0.0);
        AnchorPane.setLeftAnchor(pane, 0.0);
        AnchorPane.setRightAnchor(pane, 0.0);
    }

    @FXML
    private void showWebBrowser() throws IOException {
        AnchorPane pane = FXMLLoader.load(getClass().getResource("/org/example/videoscrapper/WebBrowser.fxml"));
        contentArea.getChildren().setAll(pane);
        AnchorPane.setTopAnchor(pane, 0.0);
        AnchorPane.setBottomAnchor(pane, 0.0);
        AnchorPane.setLeftAnchor(pane, 0.0);
        AnchorPane.setRightAnchor(pane, 0.0);
    }

}
