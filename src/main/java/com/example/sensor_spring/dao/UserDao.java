package com.example.sensor_spring.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserDao {
    private final JdbcTemplate jdbc;

    public UserDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<Map<String, Object>> findByUsername(String username) {
        try {
            Map<String, Object> row = jdbc.queryForMap(
                    "SELECT user_id, username, password FROM users WHERE username=?",
                    username
            );
            return Optional.of(row);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Map<String, Object>> findById(long userId) {
        try {
            Map<String, Object> row = jdbc.queryForMap(
                    "SELECT user_id, username, email, phone, qq, wechat, point FROM users WHERE user_id=?",
                    userId
            );
            return Optional.of(row);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean usernameExists(String username) {
        Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE username=?", Integer.class, username);
        return c != null && c > 0;
    }

    public boolean emailExists(String email, Long excludeUserId) {
        if (email == null) return false;
        if (excludeUserId == null) {
            Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE email=?", Integer.class, email);
            return c != null && c > 0;
        } else {
            Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE email=? AND user_id<>?", Integer.class, email, excludeUserId);
            return c != null && c > 0;
        }
    }

    public boolean phoneExists(String phone, Long excludeUserId) {
        if (phone == null) return false;
        if (excludeUserId == null) {
            Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE phone=?", Integer.class, phone);
            return c != null && c > 0;
        } else {
            Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE phone=? AND user_id<>?", Integer.class, phone, excludeUserId);
            return c != null && c > 0;
        }
    }

    public long createUser(String username, String hashedPw, String email, String phone, String qq, String wechat) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO users (username, password, email, phone, qq, wechat, point) VALUES (?,?,?,?,?,?,0)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, username);
            ps.setString(2, hashedPw);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setString(5, qq);
            ps.setString(6, wechat);
            return ps;
        }, kh);
        Number key = kh.getKey();
        return key == null ? -1L : key.longValue();
    }

    public void updateLoginStatusRowInit(long userId) {
        jdbc.update("INSERT INTO user_login_status (user_id) VALUES (?)", userId);
    }

    public void setLoginStatus(long userId, boolean login) {
        jdbc.update("UPDATE user_login_status SET login_status=? WHERE user_id=?", login ? 1 : 0, userId);
    }

    public List<Map<String, Object>> listAllUsers() {
        return jdbc.queryForList("SELECT user_id, username, email, phone, point, qq, wechat FROM users");
    }

    public void updatePassword(long userId, String newHashed) {
        jdbc.update("UPDATE users SET password=? WHERE user_id=?", newHashed, userId);
    }

    public int deleteUserCascade(long userId) {
        jdbc.update("DELETE FROM user_login_status WHERE user_id=?", userId);
        return jdbc.update("DELETE FROM users WHERE user_id=?", userId);
    }

    public void updateProfile(long userId, Map<String, Object> updates) {
        if (updates == null || updates.isEmpty()) return;
        StringBuilder sb = new StringBuilder("UPDATE users SET ");
        boolean first = true;
        for (String k : updates.keySet()) {
            if (!first) sb.append(", ");
            sb.append(k).append("=?");
            first = false;
        }
        sb.append(" WHERE user_id=?");
        List<Object> params = new java.util.ArrayList<>(updates.values());
        params.add(userId);
        jdbc.update(sb.toString(), params.toArray());
    }
}
