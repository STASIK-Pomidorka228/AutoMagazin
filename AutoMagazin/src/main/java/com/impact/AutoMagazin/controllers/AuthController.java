package com.impact.AutoMagazin.controllers;

import com.impact.AutoMagazin.dto.CreateUserRequest;
import com.impact.AutoMagazin.dto.LoginRequest;
import com.impact.AutoMagazin.dto.RefreshRequest;
import com.impact.AutoMagazin.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody CreateUserRequest request) {
        Map<String, String> tokens = authService.register(request);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        Map<String, String> tokens = authService.login(request);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody RefreshRequest request) {
        Map<String, String> tokens = authService.refresh(request);
        return ResponseEntity.ok(tokens);
    }
}
