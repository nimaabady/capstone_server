package com.capstone.server.auth.controller;

import com.capstone.server.auth.dto.*;
import com.capstone.server.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto dto) {

        return ResponseEntity.ok(authService.registerUser(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {

        return ResponseEntity.ok(authService.loginUser(dto));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {

        authService.logout(getUserId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete() {

        authService.deleteUser(getUserId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordDto dto) {

        authService.changePassword(dto, getUserId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/username")
    public ResponseEntity<UpdateResponseDto> changeUsername(@Valid @RequestBody UpdateUsernameDto dto) {

        return ResponseEntity.ok(authService.changeUsername(dto, getUserId()));
    }

    @PutMapping("/email")
    public ResponseEntity<UpdateResponseDto> changeEmail(@Valid @RequestBody UpdateEmailDto dto) {

        return ResponseEntity.ok(authService.changeEmail(dto, getUserId()));
    }

    @PutMapping("/status")
    public ResponseEntity<UpdateUserStatusDto> updateStatus(@Valid @RequestBody UpdateUserStatusDto dto) {

        return ResponseEntity.ok(authService.updateStatus(dto, getUserId()));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(authService.getUserById(id));
    }

    @GetMapping({"/users/get-id-by-username/{username}", "/users/getIdByUsername/{username}"})
    public ResponseEntity<UserIdResponseDto> getUserIdByUsername(@PathVariable String username) {
        return ResponseEntity.ok(authService.getUserIdByUsername(username));
    }

    private UUID getUserId() {
        String userIdString = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return UUID.fromString(userIdString);
    }
}