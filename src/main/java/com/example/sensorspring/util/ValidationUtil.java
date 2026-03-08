package com.example.sensorspring.util;

import java.util.regex.Pattern;

/**
 * 验证工具类
 * 参照 Sensor_Server 的 validators.py 实现
 */
public class ValidationUtil {
    
    // 用户名正则：只允许字母、数字、下划线
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
    
    // 邮箱正则
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    // 中国大陆手机号正则
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    
    /**
     * 验证用户名
     * @param username 用户名
     * @param minLength 最小长度
     * @param maxLength 最大长度
     * @return 验证结果，null表示验证通过，否则返回错误消息
     */
    public static String validateUsername(String username, int minLength, int maxLength) {
        if (username == null || username.trim().isEmpty()) {
            return "用户名不能为空";
        }
        
        username = username.trim();
        
        if (username.length() < minLength) {
            return "用户名长度不能少于 " + minLength + " 个字符";
        }
        
        if (username.length() > maxLength) {
            return "用户名长度不能超过 " + maxLength + " 个字符";
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return "用户名只能包含字母、数字和下划线";
        }
        
        return null; // 验证通过
    }
    
    /**
     * 验证用户名（默认3-20字符）
     */
    public static String validateUsername(String username) {
        return validateUsername(username, 3, 20);
    }
    
    /**
     * 验证密码强度
     * @param password 密码
     * @param minLength 最小长度
     * @return 验证结果，null表示验证通过，否则返回错误消息
     */
    public static String validatePasswordStrength(String password, int minLength) {
        if (password == null || password.isEmpty()) {
            return "密码不能为空";
        }
        
        if (password.length() < minLength) {
            return "密码长度不能少于 " + minLength + " 个字符";
        }
        
        if (password.length() > 100) {
            return "密码长度不能超过 100 个字符";
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        
        if (!hasUpper) {
            return "密码必须包含至少一个大写字母";
        }
        
        if (!hasLower) {
            return "密码必须包含至少一个小写字母";
        }
        
        if (!hasDigit) {
            return "密码必须包含至少一个数字";
        }
        
        return null; // 验证通过
    }
    
    /**
     * 验证密码强度（默认最少6字符）
     */
    public static String validatePasswordStrength(String password) {
        return validatePasswordStrength(password, 6);
    }
    
    /**
     * 验证邮箱格式
     * @param email 邮箱地址
     * @return 验证结果，null表示验证通过，否则返回错误消息
     */
    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null; // 邮箱是可选的
        }
        
        email = email.trim();
        
        if (email.length() > 100) {
            return "邮箱地址过长";
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "邮箱格式不正确";
        }
        
        return null; // 验证通过
    }
    
    /**
     * 验证手机号格式
     * @param phone 手机号
     * @return 验证结果，null表示验证通过，否则返回错误消息
     */
    public static String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null; // 手机号是可选的
        }
        
        phone = phone.trim();
        
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return "手机号格式不正确";
        }
        
        return null; // 验证通过
    }
    
    /**
     * 验证文件大小
     * @param fileSize 文件大小（字节）
     * @param maxSize 最大允许大小（字节）
     * @return 验证结果，null表示验证通过，否则返回错误消息
     */
    public static String validateFileSize(long fileSize, long maxSize) {
        if (fileSize > maxSize) {
            return "文件大小超过限制（最大 " + formatBytes(maxSize) + "）";
        }
        return null;
    }
    
    /**
     * 格式化字节数
     */
    public static String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", size, units[unitIndex]);
    }
    
    /**
     * 安全转换为整数
     */
    public static Integer safeInt(Object value, String fieldName, Integer defaultVal, Integer minVal, Integer maxVal) {
        if (value == null) {
            return defaultVal;
        }
        
        try {
            int result;
            if (value instanceof Number) {
                result = ((Number) value).intValue();
            } else {
                result = Integer.parseInt(value.toString());
            }
            
            if (minVal != null && result < minVal) {
                throw new IllegalArgumentException(fieldName + " 不能小于 " + minVal);
            }
            
            if (maxVal != null && result > maxVal) {
                throw new IllegalArgumentException(fieldName + " 不能大于 " + maxVal);
            }
            
            return result;
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
    
    /**
     * 安全转换为字符串
     */
    public static String safeString(Object value, String fieldName, String defaultVal, Integer minLen, Integer maxLen) {
        if (value == null) {
            return defaultVal;
        }
        
        String result = value.toString().trim();
        
        if (result.isEmpty()) {
            return defaultVal;
        }
        
        if (minLen != null && result.length() < minLen) {
            throw new IllegalArgumentException(fieldName + " 长度不能少于 " + minLen + " 个字符");
        }
        
        if (maxLen != null && result.length() > maxLen) {
            throw new IllegalArgumentException(fieldName + " 长度不能超过 " + maxLen + " 个字符");
        }
        
        return result;
    }
}
