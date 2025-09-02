package com.example.sensor_spring.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class QuestionsDao {
    private final JdbcTemplate jdbc;

    public QuestionsDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<Map<String, Object>> findByUserId(long userId) {
        try {
            Map<String, Object> row = jdbc.queryForMap(
                    "SELECT question1, question2, answer1, answer2 FROM user_questions WHERE user_id=?",
                    userId
            );
            return Optional.of(row);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void upsert(long userId, Map<String, Object> cols) {
        if (cols == null || cols.isEmpty()) return;
        StringBuilder colList = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        StringBuilder updates = new StringBuilder();
        boolean first = true;
        for (String c : cols.keySet()) {
            if (!first) {
                colList.append(", ");
                placeholders.append(", ");
                updates.append(", ");
            }
            colList.append(c);
            placeholders.append("?");
            updates.append(c).append("=VALUES(").append(c).append(")");
            first = false;
        }
        String sql = "INSERT INTO user_questions (user_id, " + colList + ") VALUES (?, " + placeholders + ") "
                + "ON DUPLICATE KEY UPDATE " + updates;
        java.util.List<Object> params = new java.util.ArrayList<>();
        params.add(userId);
        params.addAll(cols.values());
        jdbc.update(sql, params.toArray());
    }
}
