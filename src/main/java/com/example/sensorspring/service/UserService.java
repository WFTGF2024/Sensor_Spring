package com.example.sensorspring.service;

import com.example.sensorspring.entity.User;
import com.example.sensorspring.events.EventPublisher;
import com.example.sensorspring.exception.BadRequestException;
import com.example.sensorspring.exception.NotFoundException;
import com.example.sensorspring.repository.UserRepository;
import com.example.sensorspring.security.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository users; private final PasswordEncoder encoder; private final EventPublisher publisher;
    public UserService(UserRepository users, PasswordEncoder encoder, EventPublisher publisher){ this.users=users; this.encoder=encoder; this.publisher=publisher; }

    @Transactional
    public User register(String username, String email, String rawPassword){
        if (users.existsByUsername(username)) throw new BadRequestException("用户名已存在");
        if (users.existsByEmail(email)) throw new BadRequestException("邮箱已存在");
        User u=new User(); u.setUsername(username); u.setEmail(email); u.setPasswordHash(encoder.encode(rawPassword)); u.setRole(Role.USER);
        u = users.save(u);
        publisher.publishUserRegistered(u);
        return u;
    }

    public User getByUsernameOrEmail(String login){
        return users.findByUsernameOrEmail(login, login).orElseThrow(() -> new NotFoundException("用户不存在"));
    }
    public User getById(Long id){
        return users.findById(id).orElseThrow(() -> new NotFoundException("用户不存在"));
    }
}
