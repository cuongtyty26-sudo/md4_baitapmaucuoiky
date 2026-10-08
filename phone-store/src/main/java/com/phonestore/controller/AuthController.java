package com.phonestore.controller;

import com.phonestore.config.AppConfig;
import com.phonestore.dto.LoginRequest;
import com.phonestore.dto.RegisterRequest;
import com.phonestore.entity.Admin;
import com.phonestore.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) { this.service = service; }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest r) {
        service.register(r);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Đăng ký thành công"));
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest r, HttpSession session) {
        Admin a = service.login(r);
        session.setAttribute(AppConfig.SESSION_KEY, a.getUsername());
        return Map.of("username", a.getUsername());
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpSession session) {
        session.invalidate();
        return Map.of("message", "Đã đăng xuất");
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(HttpSession session) {
        Object u = session.getAttribute(AppConfig.SESSION_KEY);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Chưa đăng nhập"));
        return ResponseEntity.ok(Map.of("username", u.toString()));
    }
}
