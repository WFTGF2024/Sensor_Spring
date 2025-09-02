package com.example.sensor_spring.controller;

import com.example.sensor_spring.dao.QuestionsDao;
import com.example.sensor_spring.dao.UserDao;
import com.example.sensor_spring.util.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserDao userDao;
    private final QuestionsDao questionsDao;

    public AuthController(UserDao userDao, QuestionsDao questionsDao) {
        this.userDao = userDao;
        this.questionsDao = questionsDao;
    }

    /** 注册 */
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> data) {
        String username = str(data.get("username"));
        String password = str(data.get("password"));
        if (isBlank(username) || isBlank(password)) {
            return error(HttpStatus.BAD_REQUEST, "Missing field: " + (isBlank(username) ? "username" : "password"));
        }
        String email = str(data.get("email"));
        String phone = str(data.get("phone"));
        String qq = str(data.get("qq"));
        String wechat = str(data.get("wechat"));

        if (userDao.usernameExists(username)) {
            return error(HttpStatus.CONFLICT, "Username already taken");
        }
        if (userDao.emailExists(email, null)) {
            return error(HttpStatus.CONFLICT, "Email already taken");
        }
        if (userDao.phoneExists(phone, null)) {
            return error(HttpStatus.CONFLICT, "Phone already taken");
        }

        String hashed = PasswordUtil.hash(password);
        long userId = userDao.createUser(username, hashed, email, phone, qq, wechat);
        userDao.updateLoginStatusRowInit(userId);

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Registration successful");
        res.put("user_id", userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /** 登录 */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> data,
                                                     HttpServletRequest request) {
        String username = str(data.get("username"));
        String password = str(data.get("password"));
        if (isBlank(username) || isBlank(password)) {
            return error(HttpStatus.BAD_REQUEST, "Username and password required");
        }

        Optional<Map<String, Object>> rowOpt = userDao.findByUsername(username);
        if (rowOpt.isEmpty()) {
            return error(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        Map<String, Object> row = rowOpt.get();
        long userId = ((Number) row.get("user_id")).longValue();
        String stored = (String) row.get("password");
        if (!PasswordUtil.verify(password, stored)) {
            return error(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        // ✅ 用 HttpServletRequest 创建新 session
        request.getSession().invalidate();
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute("user_id", userId);
        newSession.setAttribute("username", username);

        userDao.setLoginStatus(userId, true);

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Login successful");
        res.put("user_id", userId);
        return ResponseEntity.ok(res);
    }

    /** 登出 */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        Object uidObj = session.getAttribute("user_id");
        Long userId = uidObj == null ? null : ((Number) uidObj).longValue();
        if (userId != null) {
            userDao.setLoginStatus(userId, false);
        }
        session.invalidate();
        Map<String, Object> res = new HashMap<>();
        res.put("message", "Logged out successfully");
        res.put("user_id", userId);
        return ResponseEntity.ok(res);
    }

    /** 修改资料 */
    @PatchMapping("/user")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> data, HttpSession session) {
        Long userId = currentUser(session);
        if (userId == null) return error(HttpStatus.UNAUTHORIZED, "Authentication required");

        Map<String, Object> updates = new HashMap<>();
        if (data.containsKey("email")) {
            String email = str(data.get("email"));
            if (userDao.emailExists(email, userId)) return error(HttpStatus.CONFLICT, "Email already taken");
            updates.put("email", email);
        }
        if (data.containsKey("phone")) {
            String phone = str(data.get("phone"));
            if (userDao.phoneExists(phone, userId)) return error(HttpStatus.CONFLICT, "Phone already taken");
            updates.put("phone", phone);
        }
        if (data.containsKey("qq")) updates.put("qq", str(data.get("qq")));
        if (data.containsKey("wechat")) updates.put("wechat", str(data.get("wechat")));
        if (data.containsKey("password")) {
            updates.put("password", PasswordUtil.hash(str(data.get("password"))));
        }
        if (updates.isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "No valid fields to update");
        }
        userDao.updateProfile(userId, updates);
        Map<String, Object> res = new HashMap<>();
        res.put("message", "Profile updated successfully");
        res.put("updated", updates.keySet());
        return ResponseEntity.ok(res);
    }

    /** 获取用户信息 */
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getProfile(HttpSession session) {
        Long userId = currentUser(session);
        if (userId == null) return error(HttpStatus.UNAUTHORIZED, "Authentication required");
        var userOpt = userDao.findById(userId);
        if (userOpt.isEmpty()) return error(HttpStatus.NOT_FOUND, "User not found");
        return ResponseEntity.ok(new HashMap<>(userOpt.get()));
    }

    /** 删除账号 */
    @PostMapping("/delete_account")
    public ResponseEntity<Map<String, Object>> deleteAccount(@RequestBody Map<String, Object> data, HttpSession session) {
        Long userId = currentUser(session);
        if (userId == null) return error(HttpStatus.UNAUTHORIZED, "Authentication required");
        String pwd = str(data.get("password"));
        if (isBlank(pwd)) return error(HttpStatus.BAD_REQUEST, "Password required");

        var rowOpt = userDao.findById(userId);
        if (rowOpt.isEmpty()) return error(HttpStatus.NOT_FOUND, "User not found");
        var authRowOpt = userDao.findByUsername((String) rowOpt.get().get("username"));
        String stored = authRowOpt.map(r -> (String) r.get("password")).orElse(null);
        if (stored == null || !PasswordUtil.verify(pwd, stored)) {
            return error(HttpStatus.FORBIDDEN, "Password incorrect");
        }

        userDao.deleteUserCascade(userId);
        session.invalidate();
        Map<String, Object> res = new HashMap<>();
        res.put("message", "Account deleted successfully");
        return ResponseEntity.ok(res);
    }

    // ========== 辅助方法 ==========
    private static String str(Object o) { return o == null ? null : String.valueOf(o); }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static Long currentUser(HttpSession session) {
        Object uid = session.getAttribute("user_id");
        return uid == null ? null : ((Number) uid).longValue();
    }
    private static ResponseEntity<Map<String, Object>> error(HttpStatus status, String msg) {
        Map<String, Object> m = new HashMap<>();
        m.put("error", msg);
        return ResponseEntity.status(status).body(m);
    }
}
