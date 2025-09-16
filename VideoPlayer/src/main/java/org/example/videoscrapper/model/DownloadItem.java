package org.example.videoscrapper.model;


import javafx.beans.property.*;
import javafx.scene.image.Image;

import java.io.File;

public class DownloadItem {
    private final StringProperty fileName = new SimpleStringProperty();
    private final ObjectProperty<File> file = new SimpleObjectProperty<>();
    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private final StringProperty status = new SimpleStringProperty("Waiting...");
    private final StringProperty fileSize = new SimpleStringProperty();
    private final StringProperty duration = new SimpleStringProperty();
    private final DoubleProperty lastWatchedPosition = new SimpleDoubleProperty(0);
    private final ObjectProperty<Image> thumbnail = new SimpleObjectProperty<>();

    public DownloadItem(String fileName) { this.fileName.set(fileName); }
    public DownloadItem() {}

    // Getters and setters
    public String getFileName() { return fileName.get(); }
    public void setFileName(String name) { this.fileName.set(name); }
    public StringProperty fileNameProperty() { return fileName; }

    public File getFile() { return file.get(); }
    public void setFile(File file) { this.file.set(file); }
    public ObjectProperty<File> fileProperty() { return file; }

    public double getProgress() { return progress.get(); }
    public void setProgress(double progress) { this.progress.set(progress); }
    public DoubleProperty progressProperty() { return progress; }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }

    public String getFileSize() { return fileSize.get(); }
    public void setFileSize(String size) { this.fileSize.set(size); }
    public StringProperty fileSizeProperty() { return fileSize; }

    public String getDuration() { return duration.get(); }
    public void setDuration(String duration) { this.duration.set(duration); }
    public StringProperty durationProperty() { return duration; }

    public double getLastWatchedPosition() { return lastWatchedPosition.get(); }
    public void setLastWatchedPosition(double position) { this.lastWatchedPosition.set(position); }
    public DoubleProperty lastWatchedPositionProperty() { return lastWatchedPosition; }

    public Image getThumbnail() { return thumbnail.get(); }
    public void setThumbnail(Image img) { this.thumbnail.set(img); }
    public ObjectProperty<Image> thumbnailProperty() { return thumbnail; }

    public String getFilePath() { return (file.get() != null) ? file.get().getAbsolutePath() : null; }
    @Override
    public String toString() {
        return getFileName(); // This ensures only the file name appears
    }

}
