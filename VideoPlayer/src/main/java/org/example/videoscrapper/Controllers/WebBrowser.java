package org.example.videoscrapper.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import java.io.FileNotFoundException;

public class WebBrowser {
    @FXML private WebView webView;
    @FXML private TextField urlField;
    @FXML private TreeView<String> historyTree;

    private WebEngine engine;

    @FXML
    public void initialize() throws FileNotFoundException {
        engine = webView.getEngine();
        engine.load("https://www.google.com");

        engine.locationProperty().addListener((obs, oldLoc, newLoc) -> {
            urlField.setText(newLoc);

            urlField.setOnKeyPressed(event->{
                if(event.getCode() == KeyCode.ENTER){
                    loadPage();
                }
            });
        });
    }

    @FXML
    public void loadPage() {
        String url = urlField.getText();
        if (!url.startsWith("http")) {
            url = "https://" + url;
        }
        engine.load(url);
    }

    @FXML
    public void reloadPage() {
        engine.reload();
    }

    @FXML
    public void goBack() {
        WebHistory history = engine.getHistory();
        if (history.getCurrentIndex() > 0) {
            history.go(-1);
        }
    }

    @FXML
    public void goForward() {
        WebHistory history = engine.getHistory();
        if (history.getCurrentIndex() < history.getEntries().size() - 1) {
            history.go(1);
        }
    }
}


