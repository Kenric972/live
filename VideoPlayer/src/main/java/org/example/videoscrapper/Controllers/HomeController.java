package org.example.videoscrapper.Controllers;


import org.example.videoscrapper.DataBase.VideoDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.videoscrapper.model.DownloadItem;
import org.example.videoscrapper.model.Video;
import org.example.videoscrapper.services.DownloadTask;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeController {

    @FXML
    private TextField urlField;

    @FXML
    private Label statusLabel;

    private static final ExecutorService downloadExecutor = Executors.newFixedThreadPool(4);

    private Library libraryController; // Reference to existing Library controller

    private final File downloadsFolder = new File(System.getProperty("user.home"), "Downloads/MyAppVideos");

    /** Called by MainController to set the existing Library controller */
    public void setLibraryController(Library library) {
        this.libraryController = library;
    }

    @FXML
    public void startDownload() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            showError("Missing URL", "Please enter a video URL.");
            return;
        }

        // Validate URL format
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            showError("Invalid URL", "The URL format is not valid.");
            return;
        }

        // Ensure downloads folder exists
        if (!downloadsFolder.exists() && !downloadsFolder.mkdirs()) {
            showError("Folder Error", "Could not create downloads folder: " + downloadsFolder.getAbsolutePath());
            return;
        }

        // Create temporary DownloadItem for progress tracking
        DownloadItem item = new DownloadItem("Preparing...");
        item.setStatus("Queued");
        item.setProgress(0.0);

        Platform.runLater(() -> {
            DownloadManager.downloads.add(item);
            statusLabel.setText("Starting download...");

            // Create DownloadTask
            DownloadTask task = new DownloadTask(url, downloadsFolder, item);

            // Bind progress and status
            task.progressProperty().addListener((obs, oldVal, newVal) -> item.setProgress(newVal.doubleValue()));
            task.messageProperty().addListener((obs, oldVal, newVal) -> item.setStatus(newVal));

            // Task lifecycle
            task.setOnRunning(e -> {
                item.setStatus("Downloading...");
                statusLabel.setText("Downloading...");
            });

            task.setOnSucceeded(e -> {
                File downloadedFile = task.getDownloadedFile();
                if (downloadedFile != null && downloadedFile.exists()) {
                    item.setFile(downloadedFile);
                    item.setFileName(downloadedFile.getName());
                    item.setProgress(1.0);
                    item.setStatus("Completed");
                    statusLabel.setText("Download completed!");

                    // Create Video object
                    Video video = new Video();
                    video.setTitle(downloadedFile.getName());
                    video.setFilePath(downloadedFile.getAbsolutePath());

                    // Save to database
                    VideoDAO videoDAO = new VideoDAO();
                    videoDAO.save(video);

                    // Update Library UI
                    if (libraryController != null) {
                        libraryController.addDownloadedVideo(downloadedFile);
                    }

                } else {
                    handleDownloadFailure(item, "Download completed, but file was not found.");
                }
            });


            task.setOnFailed(e -> {
                Throwable ex = task.getException();
                String reason = (ex != null) ? ex.getMessage() : "Unknown error occurred.";
                handleDownloadFailure(item, reason);
            });

            // Submit task to executor
            downloadExecutor.submit(task);
        });
    }

    /** Show error in both label and popup */
    private void showError(String title, String message) {
        statusLabel.setText(message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Unified handler for failed downloads */
    private void handleDownloadFailure(DownloadItem item, String reason) {
        item.setStatus("Failed");
        item.progressProperty().unbind();
        statusLabel.setText("Download failed!");
        showError("Download Failed", reason);
    }
}
