package org.example.videoscrapper.services;


import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.*;

import org.example.videoscrapper.model.DownloadItem;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class DownloadTask extends Task<Void> {

    private final String videoUrl;
    private final File outputFolder;
    private final DownloadItem item;
    private File downloadedFile; // downloaded file reference
    private volatile boolean cancelled = false; // cancel flag
    private Process process; // yt-dlp process reference

    public DownloadTask(String videoUrl, File outputFolder, DownloadItem item) {
        this.videoUrl = videoUrl;
        this.outputFolder = outputFolder;
        this.item = item;
    }

    /** Getter for the downloaded file */
    public File getDownloadedFile() {
        return downloadedFile;
    }

    /** Cancel the ongoing yt-dlp process */
    public void cancelDownload() {
        cancelled = true;
        if (process != null) process.destroy();
    }

    @Override
    protected Void call() throws Exception {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "yt-dlp",
                    "-o", outputFolder.getAbsolutePath() + "/%(title)s.%(ext)s",
                    "--newline",
                    videoUrl
            );
            pb.redirectErrorStream(true);
            process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (cancelled) {
                        Platform.runLater(() -> item.setStatus("Cancelled"));
                        return null;
                    }

                    double progress = parseProgress(line);
                    if (progress >= 0) {
                        updateProgress(progress, 1.0);
                        Platform.runLater(() -> item.setStatus("Downloading..."));
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0 && !cancelled) {
                downloadedFile = findNewestFile(outputFolder);
                if (downloadedFile != null) {
                    Platform.runLater(() -> updateItemMetadata(downloadedFile));
                }
            } else if (!cancelled) {
                Platform.runLater(() -> item.setStatus("Failed"));
            }

            return null;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /** Returns progress 0-1 or -1 if not found in line */
    private double parseProgress(String line) {
        try {
            if (!line.contains("%")) return -1;
            int percentIndex = line.indexOf('%');
            int start = line.lastIndexOf(' ', percentIndex - 1);
            String percentStr = line.substring(start + 1, percentIndex).trim();
            return Double.parseDouble(percentStr) / 100.0;
        } catch (Exception e) {
            return -1;
        }
    }

    /** Updates DownloadItem metadata */
    private void updateItemMetadata(File file) {
        item.setFile(file);
        item.setFileName(file.getName());
        item.setFileSize(readableFileSize(file.length()));
        item.setThumbnail(new Image(file.toURI().toString(), 100, 70, true, true));
        item.setStatus("Completed");

        try {
            Media media = new Media(file.toURI().toString());
            MediaPlayer player = new MediaPlayer(media);
            player.setOnReady(() -> {
                item.setDuration(formatDuration(media.getDuration().toSeconds()));
                player.dispose();
            });
        } catch (Exception e) {
            item.setDuration("Unknown");
        }
    }

    private String readableFileSize(long size) {
        try {
            if (size <= 0) return "0 B";
            String[] units = {"B", "KB", "MB", "GB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return new java.text.DecimalFormat("#,##0.#")
                    .format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String formatDuration(double seconds) {
        int mins = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%d:%02d", mins, secs);
    }

    private File findNewestFile(File folder) {
        try {
            File[] files = folder.listFiles();
            if (files == null || files.length == 0) return null;
            File newest = files[0];
            for (File f : files) {
                if (f.lastModified() > newest.lastModified()) newest = f;
            }
            return newest;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
