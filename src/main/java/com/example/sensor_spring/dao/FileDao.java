package com.example.sensor_spring.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class FileDao {
    private final JdbcTemplate jdbc;
    public FileDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public boolean fileHashExists(String fileHash) {
        Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM files WHERE file_hash=?", Integer.class, fileHash);
        return c != null && c > 0;
    }

    public long insertFile(long userId, String fileName, String filePath, String description,
                           String permission, String fileHash, long fileSize) {
        jdbc.update(
            "INSERT INTO files (user_id, file_name, file_path, description, file_permission, file_hash, file_size) VALUES (?,?,?,?,?,?,?)",
            userId, fileName, filePath, description, permission, fileHash, fileSize
        );
        Long id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return id == null ? -1L : id;
    }

    public List<Map<String, Object>> listFiles(long userId) {
        return jdbc.queryForList(
            "SELECT file_id, file_name, updated_at, description, file_permission, file_hash, file_size FROM files WHERE user_id = ? ORDER BY updated_at DESC",
            userId
        );
    }

    public Optional<Map<String, Object>> findByIdAndUser(long fileId, long userId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT file_name, file_path, file_permission FROM files WHERE file_id=? AND user_id=?",
                fileId, userId
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public void updateMeta(long fileId, String newName, String newPath, String newPerm) {
        StringBuilder sb = new StringBuilder("UPDATE files SET ");
        java.util.List<Object> params = new java.util.ArrayList<>();
        boolean first = true;
        if (newName != null) {
            if (!first) sb.append(", ");
            sb.append("file_name=?");
            params.add(newName);
            first = false;
        }
        if (newPath != null) {
            if (!first) sb.append(", ");
            sb.append("file_path=?");
            params.add(newPath);
            first = false;
        }
        if (newPerm != null) {
            if (!first) sb.append(", ");
            sb.append("file_permission=?");
            params.add(newPerm);
        }
        sb.append(" WHERE file_id=?");
        params.add(fileId);
        jdbc.update(sb.toString(), params.toArray());
    }

    public void deleteById(long fileId) {
        jdbc.update("DELETE FROM files WHERE file_id=?", fileId);
    }
}
