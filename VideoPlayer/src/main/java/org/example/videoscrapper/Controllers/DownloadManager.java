package org.example.videoscrapper.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.videoscrapper.model.DownloadItem;

public class DownloadManager {
    public static final ObservableList<DownloadItem> downloads = FXCollections.observableArrayList();
}
