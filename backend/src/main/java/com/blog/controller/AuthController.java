package com.blog.controller;

import com.blog.dto.LoginRequest;
import com.blog.dto.LoginResponse;
import com.blog.entity.User;
import com.blog.service.UserService;
import com.blog.util.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByUsername(request.getUsername());

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("{\"error\": \"Invalid username or password\"}");
        }

        String token = jwtUtils.generateToken(user.getUsername());
        return ResponseEntity.ok(new LoginResponse(token, user.getUsername()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody LoginRequest request) {
        if (userService.findByUsername(request.getUsername()) != null) {
            return ResponseEntity.status(409).body("{\"error\": \"Username already exists\"}");
        }

        User user = userService.createUser(request.getUsername(), request.getPassword());
        String token = jwtUtils.generateToken(user.getUsername());
        return ResponseEntity.status(201).body(new LoginResponse(token, user.getUsername()));
    }
}
