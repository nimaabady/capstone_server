package com.capstone.server.auth.controller;

import com.capstone.server.auth.dto.LoginRequestDto;
import com.capstone.server.auth.dto.LoginResponseDto;
import com.capstone.server.auth.dto.RegisterRequestDto;
import com.capstone.server.auth.dto.RegisterResponseDto;
import com.capstone.server.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {

        return ResponseEntity.ok(authService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {

        return ResponseEntity.ok(authService.loginUser(request));
    }
}
