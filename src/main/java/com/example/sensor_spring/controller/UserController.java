package com.example.sensor_spring.controller;

import com.example.sensor_spring.dao.UserDao;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {
    private final UserDao userDao;

    public UserController(UserDao userDao) { this.userDao = userDao; }

    @GetMapping("/users")
    public List<Map<String, Object>> listUsers() { return userDao.listAllUsers(); }
}
