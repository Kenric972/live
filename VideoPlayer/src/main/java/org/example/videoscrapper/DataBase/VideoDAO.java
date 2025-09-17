package org.example.videoscrapper.DataBase;

import org.example.videoscrapper.model.Video;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VideoDAO {
    public void save(Video video) {
        String sql = "INSERT INTO videos(title, file_path) VALUES (?, ?)";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, video.getTitle());
            stmt.setString(2, video.getFilePath());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Video> findAll() {
        List<Video> videos = new ArrayList<>();
        String sql = "SELECT * FROM videos";

        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Video video = new Video(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("file_path")
                );
                videos.add(video);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return videos;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM videos WHERE id = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
