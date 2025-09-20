package com.example.sensorspring.controller;

import com.example.sensorspring.entity.User;
import com.example.sensorspring.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository users;
    public UserController(UserRepository users){ this.users=users; }

    @GetMapping("/me")
    public ResponseEntity<User> me(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(users.findByUsername(username).orElseThrow());
    }

    @GetMapping @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> all(){ return ResponseEntity.ok(users.findAll()); }
}
