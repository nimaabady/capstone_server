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

    @PostMapping("/logout/{id}")
    public ResponseEntity<Void> logout(@PathVariable UUID id) {

        authService.logout(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {

        authService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/password/{id}")
    public ResponseEntity<Void> updatePassword(@PathVariable UUID id,
                                               @Valid @RequestBody UpdatePasswordDto dto) {

        authService.changePassword(dto, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/username/{id}")
    public ResponseEntity<UpdateResponseDto> changeUsername(@PathVariable UUID id,
                                                            @Valid @RequestBody UpdateUsernameDto dto) {

        return ResponseEntity.ok(authService.changeUsername(dto, id));
    }

    @PutMapping("/email/{id}")
    public ResponseEntity<UpdateResponseDto> changeEmail(@PathVariable UUID id,
                                                         @Valid @RequestBody UpdateEmailDto dto) {

        return ResponseEntity.ok(authService.changeEmail(dto, id));
    }
}