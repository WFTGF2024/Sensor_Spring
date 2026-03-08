package com.example.sensorspring.service;

import com.example.sensorspring.entity.User;
import com.example.sensorspring.events.EventPublisher;
import com.example.sensorspring.exception.BadRequestException;
import com.example.sensorspring.exception.NotFoundException;
import com.example.sensorspring.repository.UserRepository;
import com.example.sensorspring.security.Role;
import com.example.sensorspring.util.ValidationUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务
 * 参照 Sensor_Server 的 auth_service.py 改进
 */
@Service
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final EventPublisher publisher;
    
    public UserService(UserRepository users, PasswordEncoder encoder, EventPublisher publisher) {
        this.users = users;
        this.encoder = encoder;
        this.publisher = publisher;
    }

    /**
     * 用户注册
     * 添加了用户名、密码、邮箱、手机号验证
     */
    @Transactional
    public User register(String username, String email, String rawPassword) {
        return register(username, email, rawPassword, null, null, null);
    }
    
    /**
     * 用户注册（完整版）
     * 参照 Sensor_Server 的 register 方法实现
     */
    @Transactional
    public User register(String username, String email, String rawPassword, String phone, String qq, String wechat) {
        // 验证用户名
        String usernameError = ValidationUtil.validateUsername(username);
        if (usernameError != null) {
            throw new BadRequestException(usernameError);
        }
        
        // 验证密码强度
        String passwordError = ValidationUtil.validatePasswordStrength(rawPassword);
        if (passwordError != null) {
            throw new BadRequestException(passwordError);
        }
        
        // 验证邮箱
        String emailError = ValidationUtil.validateEmail(email);
        if (emailError != null) {
            throw new BadRequestException(emailError);
        }
        
        // 验证手机号
        String phoneError = ValidationUtil.validatePhone(phone);
        if (phoneError != null) {
            throw new BadRequestException(phoneError);
        }
        
        // 检查唯一性
        if (users.existsByUsername(username)) {
            throw new BadRequestException("用户名 '" + username + "' 已被占用");
        }
        if (email != null && !email.trim().isEmpty() && users.existsByEmail(email)) {
            throw new BadRequestException("邮箱 '" + email + "' 已被注册");
        }
        if (phone != null && !phone.trim().isEmpty() && users.existsByPhone(phone)) {
            throw new BadRequestException("手机号 '" + phone + "' 已被注册");
        }
        
        // 创建用户
        User u = new User();
        u.setUsername(username.trim());
        u.setEmail(email != null ? email.trim() : null);
        u.setPasswordHash(encoder.encode(rawPassword));
        u.setPhone(phone != null && !phone.trim().isEmpty() ? phone.trim() : null);
        u.setQq(qq != null && !qq.trim().isEmpty() ? qq.trim() : null);
        u.setWechat(wechat != null && !wechat.trim().isEmpty() ? wechat.trim() : null);
        u.setRole(Role.USER);
        
        u = users.save(u);
        publisher.publishUserRegistered(u);
        return u;
    }
    
    /**
     * 更新用户资料
     * 参照 Sensor_Server 的 update_profile 方法实现
     */
    @Transactional
    public User updateProfile(Long userId, String email, String phone, String qq, String wechat) {
        User user = getById(userId);
        
        // 验证邮箱
        String emailError = ValidationUtil.validateEmail(email);
        if (emailError != null) {
            throw new BadRequestException(emailError);
        }
        
        // 验证手机号
        String phoneError = ValidationUtil.validatePhone(phone);
        if (phoneError != null) {
            throw new BadRequestException(phoneError);
        }
        
        // 检查邮箱冲突
        if (email != null && !email.trim().isEmpty()) {
            users.findByEmail(email).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(userId)) {
                    throw new BadRequestException("邮箱 '" + email + "' 已被其他用户注册");
                }
            });
        }
        
        // 检查手机号冲突
        if (phone != null && !phone.trim().isEmpty()) {
            users.findByPhone(phone).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(userId)) {
                    throw new BadRequestException("手机号 '" + phone + "' 已被其他用户注册");
                }
            });
        }
        
        // 更新用户信息
        if (email != null) user.setEmail(email.trim().isEmpty() ? null : email.trim());
        if (phone != null) user.setPhone(phone.trim().isEmpty() ? null : phone.trim());
        if (qq != null) user.setQq(qq.trim().isEmpty() ? null : qq.trim());
        if (wechat != null) user.setWechat(wechat.trim().isEmpty() ? null : wechat.trim());
        
        return users.save(user);
    }
    
    /**
     * 修改密码
     * 参照 Sensor_Server 的 change_password 方法实现
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = getById(userId);
        
        // 验证当前密码
        if (!encoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BadRequestException("当前密码错误");
        }
        
        // 验证新密码强度
        String passwordError = ValidationUtil.validatePasswordStrength(newPassword);
        if (passwordError != null) {
            throw new BadRequestException(passwordError);
        }
        
        // 检查新密码不能与旧密码相同
        if (encoder.matches(newPassword, user.getPasswordHash())) {
            throw new BadRequestException("新密码不能与当前密码相同");
        }
        
        user.setPasswordHash(encoder.encode(newPassword));
        users.save(user);
    }
    
    /**
     * 删除账户
     * 参照 Sensor_Server 的 delete_account 方法实现
     */
    @Transactional
    public void deleteAccount(Long userId, String password) {
        User user = getById(userId);
        
        // 验证密码
        if (!encoder.matches(password, user.getPasswordHash())) {
            throw new BadRequestException("密码错误");
        }
        
        users.delete(user);
    }
    
    /**
     * 检查用户名是否可用
     */
    public boolean isUsernameAvailable(String username) {
        String error = ValidationUtil.validateUsername(username);
        if (error != null) {
            return false;
        }
        return !users.existsByUsername(username);
    }

    public User getByUsernameOrEmail(String login) {
        return users.findByUsernameOrEmail(login, login).orElseThrow(() -> new NotFoundException("用户不存在"));
    }
    
    public User getById(Long id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("用户不存在"));
    }
}
