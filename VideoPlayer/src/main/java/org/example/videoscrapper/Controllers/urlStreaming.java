package org.example.videoscrapper.Controllers;


import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class urlStreaming {
    @FXML
    private Slider progressSlider;
    @FXML
    private TextField urlField;
    @FXML
    private StackPane videoContainer;
    @FXML
    private VBox settingsPanel;
    @FXML
    private ComboBox<String> resolutionSelector;
    @FXML
    private Button playButton, rewind, fastforward, settingsButton;
    @FXML
    private MediaView mediaView;
    @FXML
    private Label statusLabel;
    @FXML
    Label timeLabel;
    @FXML
    AnchorPane rootPane;
    private MediaPlayer mediaPlayer;
    private String videoPageUrl = null;

   private ImageView playIcon, pauseIcon, fastForwardIcon, rewindIcon;

    // Map resolution label -> yt-dlp format code
   private final Map<String, String> formatCodeMap = new HashMap<>();

   private double lastPositionSeconds = 0;

@FXML
private void initialize() {
    try {
        mediaView.setPickOnBounds(false);

        mediaView.setMouseTransparent(true);

        playIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/play.png"))));

        playIcon.setFitWidth(50);
        playIcon.setFitHeight(50);

        pauseIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/pause.png"))));

        pauseIcon.setFitWidth(50);
        pauseIcon.setFitHeight(50);

        fastForwardIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/fastforward.png"))));

        fastForwardIcon.setFitHeight(50);
        fastForwardIcon.setFitWidth(50);

        rewindIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/backward.png"))));

        rewindIcon.setFitWidth(50);
        rewindIcon.setFitHeight(50);
        playButton.setGraphic(playIcon);
        fastforward.setGraphic(fastForwardIcon);

        rewind.setGraphic(rewindIcon);

        videoContainer.setOnMouseEntered(e -> {
            playButton.setVisible(true);

            fastforward.setVisible(true); rewind.setVisible(true);

            resolutionSelector.setVisible(true); });

        videoContainer.setOnMouseExited(e -> { playButton.setVisible(false);

            fastforward.setVisible(false); rewind.setVisible(false);

            resolutionSelector.setVisible(false); });

        playButton.setOnAction(e -> handlePlayPause());

        fastforward.setOnAction(e -> fastForward());

        rewind.setOnAction(e -> rewind());

        resolutionSelector.setVisible(false);

        resolutionSelector.setOnAction(e -> {
            if (mediaPlayer != null && videoPageUrl != null) {
                String selected = resolutionSelector.getSelectionModel().getSelectedItem();
            if (selected != null) { switchResolution(formatCodeMap.get(selected)); } } });

        // starts playing the video once the enter key is pressed
        urlField.setOnKeyPressed(event -> { if (event.getCode() == KeyCode.ENTER) {
            startPlayingDefaultResolution();
        } });

        settingsPanel.setVisible(false);

        settingsPanel.setManaged(false);


        settingsPanel.setOpacity(0);
        // Close settings when clicking outside
        rootPane.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            // Check if click is outside settings panel and button
            if (!settingsPanel.isHover() && !settingsButton.isHover())
            { fadeSettingsPanel(false);
            }
        });
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}

// fetches the video and default resolution from the url
private void startPlayingDefaultResolution() {
    try {
        videoPageUrl = urlField.getText().trim();

        if (videoPageUrl.isEmpty()) {
            statusLabel.setText("Please enter a video URL.");
            return;
        }
        playButton.setDisable(true);
        resolutionSelector.getItems().clear();
        formatCodeMap.clear();
        statusLabel.setText("Fetching default resolution and starting playback...");

        Task<Void> metadataTask = new Task<>() {
            @Override protected Void call() throws Exception {
                ProcessBuilder pb = new ProcessBuilder("yt-dlp", "-j", videoPageUrl);
                Process process = pb.start();

                StringBuilder json = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                String line; while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
            }
            process.waitFor();
            JSONObject data = new JSONObject(json.toString());

            JSONArray formats = data.getJSONArray("formats");

            for (int i = 0; i < formats.length(); i++) {
                JSONObject format = formats.getJSONObject(i);

                String formatId = format.getString("format_id");

                String resolution = format.optString("resolution", "unknown");

                if (!resolution.equals("audio") && !resolution.equals("unknown")) {

                    formatCodeMap.put(resolution, formatId);
                }
            }
            String bestUrl = data.getString("url");

            Platform.runLater(() -> {
                resolutionSelector.getItems().clear();
                resolutionSelector.getItems().addAll(formatCodeMap.keySet());
                resolutionSelector.getSelectionModel().selectFirst();
                resolutionSelector.setVisible(true);
                playVideo(bestUrl);
                // Play only once here
                statusLabel.setText("Playing video...");

                playButton.setDisable(false);
            });
            return null;
         }
        };
        metadataTask.setOnFailed(e -> Platform.runLater(() -> {
            statusLabel.setText("Error fetching video info: " + metadataTask.getException().getMessage());
            playButton.setDisable(false);
        }));
        new Thread(metadataTask).start();
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}

// collects all the resolution settings
private void fetchAllFormatsInBackground() {
    try {
        Task<Void> fetchFormatsTask = new Task<>()
        {
            @Override protected Void call() throws Exception {
                ProcessBuilder pb = new ProcessBuilder("yt-dlp", "-F", videoPageUrl);
                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line; while ((line = reader.readLine()) != null) {
                        // Parse format lines like: // 137 mp4 1080p ...
                        if (line.matches("\\s*\\d+.*")) { String[] parts = line.trim().split("\\s+");
                            if(parts.length >= 3){ String formatCode = parts[0]; String resolution = parts[2];
                                if (!resolution.equals("audio") && !resolution.equals("unknown")) { formatCodeMap.put(resolution, formatCode); } } } } }

                process.waitFor(); return null;
            }
        };
        fetchFormatsTask.setOnSucceeded(e -> {
            if (formatCodeMap.isEmpty()) {
                statusLabel.setText("No formats found.");
                return; }

            Platform.runLater(() -> {
                resolutionSelector.getItems().clear();
                resolutionSelector.getItems().addAll(formatCodeMap.keySet());
                resolutionSelector.getSelectionModel().selectFirst();
                resolutionSelector.setVisible(true);
                statusLabel.setText("Resolutions loaded."); }); });

        fetchFormatsTask.setOnFailed(e -> { Platform.runLater(() ->
                statusLabel.setText("Failed to load resolutions: " + fetchFormatsTask.getException().getMessage()));

        });
        new Thread(fetchFormatsTask).start();
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}

    private void switchResolution(String formatCode) {
        try {
            if (mediaPlayer == null) return;

            lastPositionSeconds = mediaPlayer.getCurrentTime().toSeconds();
            statusLabel.setText("Switching resolution...");
            playButton.setDisable(true);

            Task<String> getUrlTask = new Task<>() {
                @Override protected String call() throws Exception {

                ProcessBuilder pb = new ProcessBuilder("yt-dlp", "-g", "-f", formatCode, videoPageUrl);
                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String directUrl = reader.readLine();
                    process.waitFor();

                    return directUrl; } } };

            getUrlTask.setOnSucceeded(e -> {
                String streamUrl = getUrlTask.getValue();

                if (streamUrl == null || streamUrl.isEmpty()) {
                    statusLabel.setText("Failed to get stream URL for selected resolution.");
                    playButton.setDisable(false);
                    return;
                }
                if (mediaPlayer != null)
                    { mediaPlayer.stop();
                        mediaPlayer.dispose();
                    }

                Media media = new Media(streamUrl);

                mediaPlayer = new MediaPlayer(media);
                mediaView.setMediaPlayer(mediaPlayer);
                mediaPlayer.setOnReady(() -> {
                    mediaPlayer.seek(Duration.seconds(lastPositionSeconds));
                    mediaPlayer.play();
                    statusLabel.setText("Playing...");
                    playButton.setDisable(false);
                    playButton.setGraphic(pauseIcon);
                });
            });

            getUrlTask.setOnFailed(e ->
            { statusLabel.setText("Error getting stream URL: " + getUrlTask.getException().getMessage());

                playButton.setDisable(false);
            });

            new Thread(getUrlTask).start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void playVideo(String videoUrl) {
        // Dispose old player if exists
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        // Create new MediaPlayer
        Media media = new Media(videoUrl);
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);

        // Update slider and time label
        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!progressSlider.isValueChanging()) {
                progressSlider.setValue(newTime.toSeconds());
            }
            Duration total = mediaPlayer.getTotalDuration();
            timeLabel.setText(formatTime(newTime) + " / " + formatTime(total));
        });

        // Set slider max when ready
        mediaPlayer.setOnReady(() -> {
            progressSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
        });
        
        // Start playing
        mediaPlayer.play();
        playButton.setGraphic(pauseIcon);
    }


// This function formats the playback time
    private String formatTime(Duration duration) {
        int totalSeconds = (int) Math.floor(duration.toSeconds());
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

// This function is responsible for resuming or pausing the video
private void handlePlayPause() {
    if (mediaPlayer == null) {
        statusLabel.setText("No video loaded.");
        return;
    }
    MediaPlayer.Status status = mediaPlayer.getStatus();
    if (status == MediaPlayer.Status.PLAYING) {
        mediaPlayer.pause();
        playButton.setGraphic(playIcon);
        statusLabel.setText("Paused.");
    } else {
        mediaPlayer.play();
        playButton.setGraphic(pauseIcon);
        statusLabel.setText("Playing...");
    }
}

// function to rewind the video 10 by ten seconds
@FXML
private void rewind() {
    if(mediaPlayer == null || mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING)
        return;
    Duration currentTime = mediaPlayer.getCurrentTime();
    Duration skip = Duration.seconds(10);
    Duration newTime = currentTime.subtract(skip);

    if(newTime.lessThan(mediaPlayer.getTotalDuration())){
        newTime = mediaPlayer.getCurrentTime();
    } mediaPlayer.seek(skip);
}

    //Function to skip 10 seconds of the video
    @FXML
    private void fastForward() {
    if (mediaPlayer == null || mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING)

        return;

    Duration currentTime = mediaPlayer.getCurrentTime();

    Duration skip = Duration.seconds(10);

    Duration newTime = currentTime.add(skip);

    if(newTime.greaterThan(mediaPlayer.getTotalDuration())){
        newTime = mediaPlayer.getCurrentTime();
    } mediaPlayer.seek(newTime);
}

// This function is responsible for the visibility of the settings panel
@FXML
private void toggleSettings() {
    boolean currentlyVisible = settingsPanel.isVisible();
    fadeSettingsPanel(!currentlyVisible);

}
private void fadeSettingsPanel(boolean show) {
    FadeTransition ft = new FadeTransition(Duration.millis(300), settingsPanel);

    if (show) { settingsPanel.setVisible(true);
        settingsPanel.setManaged(true);
        ft.setFromValue(0.0); ft.setToValue(1.0);

    } else {

        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {

            settingsPanel.setVisible(false);

            settingsPanel.setManaged(false);
        });
    } ft.play();
  }

}


