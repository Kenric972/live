package org.example.videoscrapper.Controllers;


import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import javafx.application.Platform;

import java.io.File;

import org.example.videoscrapper.model.DownloadItem;
import org.example.videoscrapper.services.DownloadTask;

public class ModernDownloadItemCell extends ListCell<DownloadItem> {

    private final HBox container = new HBox(10);
    private final ImageView thumbnailView = new ImageView();
    private final VBox infoBox = new VBox(5);
    private final Label nameLabel = new Label();
    private final HBox sizeDurationBox = new HBox(10);
    private final Label fileSizeLabel = new Label();
    private final Label durationLabel = new Label();
    private final StackPane progressPane = new StackPane();
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Label progressText = new Label("0%");
    private final Label statusLabel = new Label();

    private DownloadTask currentTask;

    public ModernDownloadItemCell() {
        container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        thumbnailView.setFitWidth(120);
        thumbnailView.setFitHeight(70);
        thumbnailView.setPreserveRatio(true);

        progressBar.setPrefWidth(200);
        progressText.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        progressPane.getChildren().addAll(progressBar, progressText);
        StackPane.setAlignment(progressText, javafx.geometry.Pos.CENTER);

        sizeDurationBox.getChildren().addAll(fileSizeLabel, durationLabel);
        infoBox.getChildren().addAll(nameLabel, sizeDurationBox, progressPane, statusLabel);

        container.getChildren().addAll(thumbnailView, infoBox);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // Right-click context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem downloadItem = new MenuItem("Download/Resume");
        MenuItem pauseItem = new MenuItem("Pause");
        MenuItem deleteItem = new MenuItem("Delete");

        downloadItem.setOnAction(e -> startOrResumeDownload(getItem()));
        pauseItem.setOnAction(e -> pauseDownload(getItem()));
        deleteItem.setOnAction(e -> deleteItem(getItem()));

        contextMenu.getItems().addAll(downloadItem, pauseItem, deleteItem);
        setContextMenu(contextMenu);
    }

    @Override
    protected void updateItem(DownloadItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            nameLabel.textProperty().bind(item.fileNameProperty());
            fileSizeLabel.textProperty().bind(item.fileSizeProperty());
            durationLabel.textProperty().bind(item.durationProperty());
            progressBar.progressProperty().bind(item.progressProperty());
            progressText.textProperty().bind(item.progressProperty().multiply(100).asString("%.0f%%"));
            statusLabel.textProperty().bind(item.statusProperty());

            setGraphic(container);
        }
    }

    // --- Context menu actions ---
    private void startOrResumeDownload(DownloadItem item) {
        if (item == null) return;

        item.setStatus("Downloading...");
        if (currentTask == null || currentTask.isDone()) {
            File outputFolder = item.getFile().getParentFile();
            currentTask = new DownloadTask(item.getFilePath(), outputFolder, item);
            new Thread(currentTask).start();
        }
    }

    private void pauseDownload(DownloadItem item) {
        if (item == null) return;
        if (currentTask != null) currentTask.cancelDownload();
        item.setStatus("Paused");
    }

    private void deleteItem(DownloadItem item) {
        if (item == null) return;
        if (currentTask != null) currentTask.cancelDownload();

        File file = item.getFile();
        if (file != null && file.exists()) file.delete();

        Platform.runLater(() -> getListView().getItems().remove(item));
    }
}
