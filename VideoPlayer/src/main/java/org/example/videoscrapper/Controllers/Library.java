package org.example.videoscrapper.Controllers;



import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.ListView;
import org.example.videoscrapper.model.DownloadItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Library {

    @FXML
    private ListView<DownloadItem> downloadsList;

    @FXML
    private AnchorPane contentArea;

    // Folder to store videos
    private final File videosFolder = new File(System.getProperty("user.home"), "Downloads/MyAppVideos");

    @FXML
    public void initialize() {
        // Use custom cell to display progress, file size, thumbnail, etc.
        downloadsList.setCellFactory(listView -> new ModernDownloadItemCell());
        downloadsList.setItems(DownloadManager.downloads); // ObservableList<DownloadItem>


        // Load library from backend
       loadLibraryFromFolder();

        // Double-click to play video
        // Optional: auto-play when selection changes

        downloadsList.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                DownloadItem selected = downloadsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    playSelectedVideo(selected);
                }
            }
        });

    }

    /** Load all video files from the folder into the ListView */
    private void loadLibraryFromFolder() {
        File[] files = videosFolder.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".webm");
        });

        if (files == null) return;

        // Avoid duplicates
        Set<String> existingFiles = new HashSet<>();
        downloadsList.getItems().forEach(item -> existingFiles.add(item.getFileName()));

        for (File file : files) {
            if (!existingFiles.contains(file.getName())) {
                DownloadItem item = new DownloadItem();
                item.setFile(file);
                item.setFileName(file.getName());
                item.setStatus("Downloaded");
                downloadsList.getItems().add(item);
            }
        }
    }

    /** Play the given video */
    public void playSelectedVideo(DownloadItem selected) {
        if (selected == null || selected.getFile() == null || !selected.getFile().exists()) {
            System.out.println("File missing: " + (selected != null ? selected.getFile() : "null"));
            return;
        }
        if (contentArea == null) {
            System.out.println("Content area is null!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/videoscrapper/videoPlayer.fxml"
            ));
            AnchorPane videoPane = loader.load();

            VideoPlayer controller = loader.getController();

            // Set the playlist and start index
            List<DownloadItem> playlist = new ArrayList<>(downloadsList.getItems());
            int startIndex = playlist.indexOf(selected);
            controller.setPlaylist(playlist, startIndex);

            AnchorPane.setTopAnchor(videoPane, 0.0);
            AnchorPane.setBottomAnchor(videoPane, 0.0);
            AnchorPane.setLeftAnchor(videoPane, 0.0);
            AnchorPane.setRightAnchor(videoPane, 0.0);

            contentArea.getChildren().setAll(videoPane);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Call this method after a new video download completes */
    public void addDownloadedVideo(File file) {
        if (file == null || !file.exists()) return;

        // Avoid adding duplicate
        boolean alreadyExists = downloadsList.getItems().stream()
                .anyMatch(item -> file.getName().equals(item.getFileName()));
        if (alreadyExists) return;

        DownloadItem item = new DownloadItem();
        item.setFile(file);
        item.setFileName(file.getName());
        item.setStatus("Downloaded");

        Platform.runLater(() -> downloadsList.getItems().add(item));
    }
}
