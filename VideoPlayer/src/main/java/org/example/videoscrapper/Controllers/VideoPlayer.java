package org.example.videoscrapper.Controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.example.videoscrapper.model.DownloadItem;

import java.io.File;
import java.util.List;

public class VideoPlayer {
    @FXML
    private Slider progressSlider;

    @FXML
    private StackPane videoContainer;

    @FXML
    private Button playButton, rewind, fastforward, settingsButton, next, previous;

    @FXML
    private MediaView mediaView;

    @FXML
    private Label playBackPosition;

    @FXML
    private HBox controlBar;

    private FadeTransition fadeInSlider, fadeOutSlider;
    private FadeTransition fadeInButtons, fadeOutButtons;
    private List<DownloadItem> playlist;
    private int currentIndex;


    private DownloadItem currentItem;

    @FXML
    AnchorPane rootPane;

    private MediaPlayer mediaPlayer;

    private ImageView playIcon, pauseIcon, fastForwardIcon, rewindIcon, previousIcon, nextIcon;




    @FXML
    private void initialize() {

        mediaView.fitWidthProperty().bind(videoContainer.widthProperty());
        mediaView.fitHeightProperty().bind(videoContainer.heightProperty());
        mediaView.setPreserveRatio(true);

        // --- Load icons ---
        playIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/play.png")));
        playIcon.setFitWidth(50);
        playIcon.setFitHeight(50);

        pauseIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/pause.png")));
        pauseIcon.setFitWidth(50);
        pauseIcon.setFitHeight(50);

        fastForwardIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/fastforward.png")));
        fastForwardIcon.setFitWidth(50);
        fastForwardIcon.setFitHeight(50);

        rewindIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/backward.png")));
        rewindIcon.setFitWidth(50);
        rewindIcon.setFitHeight(50);

        nextIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/next.png")));
        nextIcon.setFitWidth(50);
        nextIcon.setFitHeight(50);

        previousIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/previous.png")));
        previousIcon.setFitWidth(50);
        previousIcon.setFitHeight(50);

        // --- Assign icons to buttons ---
        playButton.setGraphic(playIcon);
        fastforward.setGraphic(fastForwardIcon);
        rewind.setGraphic(rewindIcon);
        previous.setGraphic(previousIcon); // fixed
        next.setGraphic(nextIcon);

        // --- Set initial opacity ---
        progressSlider.setOpacity(0);
        controlBar.setOpacity(0);

        // --- Create fade transitions ---
        fadeInSlider = new FadeTransition(Duration.millis(200), progressSlider);
        fadeInSlider.setFromValue(0);
        fadeInSlider.setToValue(1);

        fadeOutSlider = new FadeTransition(Duration.millis(200), progressSlider);
        fadeOutSlider.setFromValue(1);
        fadeOutSlider.setToValue(0);

        fadeInButtons = new FadeTransition(Duration.millis(200), controlBar);
        fadeInButtons.setFromValue(0);
        fadeInButtons.setToValue(1);

        fadeOutButtons = new FadeTransition(Duration.millis(200), controlBar);
        fadeOutButtons.setFromValue(1);
        fadeOutButtons.setToValue(0);

        // --- Hover behavior ---
        videoContainer.setOnMouseEntered(e -> {
            fadeOutSlider.stop();
            fadeOutButtons.stop();
            fadeInSlider.playFromStart();
            fadeInButtons.playFromStart();
        });

        videoContainer.setOnMouseExited(e -> {
            fadeInSlider.stop();
            fadeInButtons.stop();
            fadeOutSlider.playFromStart();
            fadeOutButtons.playFromStart();
        });
    }


    @FXML
    private void playPauseVideo() {
        if (mediaPlayer == null) return;

        MediaPlayer.Status status = mediaPlayer.getStatus();
        switch (status) {
            case PLAYING -> {
                mediaPlayer.pause();
                playButton.setGraphic(playIcon);
            }
            case READY, PAUSED, STOPPED, HALTED -> {
                mediaPlayer.play();
                playButton.setGraphic(pauseIcon);
            }
            default -> {} // do nothing for unknown statuses
        }
    }


    public void setPlaylist(List<DownloadItem> playlist, int startIndex) {
        if (playlist == null || playlist.isEmpty()) return; // safety check
        this.playlist = playlist;
        this.currentIndex = Math.min(startIndex, playlist.size() - 1);
        loadVideo(this.playlist.get(this.currentIndex));

    }



    public void loadVideo(DownloadItem item) {
        if (item == null || item.getFile() == null || !item.getFile().exists()) return;

        currentItem = item; // store for slider/resume position

        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }

        File file = item.getFile();
        Media media = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.setOnEndOfMedia(this::playNextVideo);

        // Slider tracking
        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!progressSlider.isValueChanging()) {
                progressSlider.setValue(newTime.toSeconds());
            }

            // Update playback position label
            String formatted = formatTime(newTime, media.getDuration());
            playBackPosition.setText(formatted);
        });


        mediaPlayer.setOnReady(() -> {
            progressSlider.setMax(media.getDuration().toSeconds());
            mediaPlayer.seek(Duration.seconds(item.getLastWatchedPosition()));
        });

        // Seek on slider drag
        progressSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
        });
        progressSlider.setOnMouseReleased(e -> mediaPlayer.seek(Duration.seconds(progressSlider.getValue())));

        mediaPlayer.play();
        playButton.setGraphic(pauseIcon);
    }

    private String formatTime(Duration elapsed, Duration total) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int hours = intElapsed / (60 * 60);
        if (hours > 0) {
            intElapsed -= hours * 60 * 60;
        }
        int minutes = intElapsed / 60;
        int seconds = intElapsed - minutes * 60;

        if (total != null && !total.isUnknown()) {
            int intTotal = (int) Math.floor(total.toSeconds());
            int totalHours = intTotal / (60 * 60);
            if (totalHours > 0) {
                intTotal -= totalHours * 60 * 60;
            }
            int totalMinutes = intTotal / 60;
            int totalSeconds = intTotal - totalMinutes * 60;

            if (hours > 0) {
                return String.format("%d:%02d:%02d / %d:%02d:%02d",
                        hours, minutes, seconds,
                        totalHours, totalMinutes, totalSeconds);
            } else {
                return String.format("%02d:%02d / %02d:%02d",
                        minutes, seconds,
                        totalMinutes, totalSeconds);
            }
        } else {
            if (hours > 0) {
                return String.format("%d:%02d:%02d", hours, minutes, seconds);
            } else {
                return String.format("%02d:%02d", minutes, seconds);
            }
        }
    }


    private static final double SEEK_INTERVAL = 10; // seconds

    @FXML
    private void rewindVideo() {
        if (mediaPlayer != null) {
            Duration currentTime = mediaPlayer.getCurrentTime();
            Duration newTime = currentTime.subtract(Duration.seconds(SEEK_INTERVAL));

            if (newTime.lessThan(Duration.ZERO)) {
                newTime = Duration.ZERO;
            }

            mediaPlayer.seek(newTime);
        }
    }

    @FXML
    private void fastForwardVideo() {
        if (mediaPlayer != null) {
            Duration currentTime = mediaPlayer.getCurrentTime();
            Duration totalDuration = mediaPlayer.getTotalDuration();
            Duration newTime = currentTime.add(Duration.seconds(SEEK_INTERVAL));

            // Ensure we don't go past end
            if (newTime.greaterThan(totalDuration)) {
                newTime = totalDuration;
            }

            mediaPlayer.seek(newTime);
        }
    }

    private void saveCurrentPosition() {
        if (currentItem != null && mediaPlayer != null) {
            currentItem.setLastWatchedPosition(mediaPlayer.getCurrentTime().toSeconds());
        }
    }

    public void playNextVideo() {
        if (playlist == null || playlist.isEmpty()) return;
        currentIndex = (currentIndex + 1) % playlist.size(); // loop back at end
        loadVideo(playlist.get(currentIndex));
    }

    public void playPrevVideo() {
        if (playlist == null || playlist.isEmpty()) return;
        currentIndex = (currentIndex - 1 + playlist.size()) % playlist.size(); // loop back
        loadVideo(playlist.get(currentIndex));
    }

    @FXML
    private void handleNext() {
        playNextVideo();
    }

    @FXML
    private void handlePrev() {
        playPrevVideo();
    }


}
