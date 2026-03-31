package com.capstone.server.auth.service;

import com.capstone.server.auth.dto.*;
import com.capstone.server.auth.exception.AuthenticationException;
import com.capstone.server.auth.exception.BadRequestException;
import com.capstone.server.auth.exception.ForbiddenException;
import com.capstone.server.auth.exception.NotFoundException;
import com.capstone.server.auth.jwt.service.JwtService;
import com.capstone.server.auth.model.User;
import com.capstone.server.auth.model.UserStatus;
import com.capstone.server.auth.repository.AuthRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthRepository _authRepository;
    private final PasswordEncoder _passwordEncoder;
    private final JwtService _jwtService;

    public AuthServiceImpl(AuthRepository authRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {

        _authRepository = authRepository;
        _passwordEncoder = passwordEncoder;
        _jwtService = jwtService;
    }

    @Override
    public RegisterResponseDto registerUser(RegisterRequestDto dto) {

        if (_authRepository.existsByEmail(dto.email())) {
            throw new BadRequestException("Email already registered.");
        }
        if (_authRepository.existsByUsername(dto.username())) {
            throw new BadRequestException("Username already taken.");
        }

        String hashedPassword = _passwordEncoder.encode(dto.password());

        User user = User.builder()
                .email(dto.email())
                .username(dto.username())
                .hashedPassword(hashedPassword)
                .status(UserStatus.Offline)
                .build();

        User saved = _authRepository.save(user);

        return new RegisterResponseDto(
                saved.getId(),
                saved.getUsername(),
                saved.getEmail(),
                new java.sql.Date(saved.getCreatedAt().getTime())
        );
    }

    @Override
    public LoginResponseDto loginUser(LoginRequestDto dto) {

        User user;

        if (isEmail(dto.usernameOrEmail())) {
            user = _authRepository.findByEmail(dto.usernameOrEmail())
                    .orElseThrow(() -> new NotFoundException("User doesn't exist."));
        } else {
            user = _authRepository.findByUsername(dto.usernameOrEmail())
                    .orElseThrow(() -> new NotFoundException("User doesn't exist."));
        }

        if (_passwordEncoder.matches(dto.password(), user.getHashedPassword())) {

            user.setStatus(UserStatus.Online);
            _authRepository.save(user);

            String token = _jwtService.generateToken(user.getId());

            return new LoginResponseDto(
                    token,
                    user.getEmail(),
                    user.getUsername(),
                    user.getStatus()
            );

        } else {
            throw new AuthenticationException("Invalid credentials.");
        }
    }

    @Override
    public void deleteUser(UUID id) {

        if (id == null) {
            throw new IllegalArgumentException("Invalid id.");
        }

        User user = _authRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User doesn't exist."));

        _authRepository.delete(user);
    }

    @Override
    public void logout(UUID id) {

        if (id == null) {
            throw new IllegalArgumentException("Invalid id.");
        }

        User user = _authRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User doesn't exist."));

        user.setStatus(UserStatus.Offline);
        _authRepository.save(user);
    }

    @Override
    public void changePassword(UpdatePasswordDto dto, UUID id) {

        if (dto.currentPassword().equals(dto.newPassword())) {
            throw new IllegalArgumentException("Passwords can't be the same.");
        }

        User user = _authRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User doesn't exist."));

        if (_passwordEncoder.matches(dto.currentPassword(), user.getHashedPassword())) {

            String hashedPassword = _passwordEncoder.encode(dto.newPassword());
            user.setHashedPassword(hashedPassword);
            _authRepository.save(user);

        } else {
            throw new ForbiddenException("Current password is incorrect.");
        }
    }


    @Override
    public UpdateResponseDto changeEmail(UpdateEmailDto dto, UUID id) {

        if (dto.currentEmail().equals(dto.newEmail())) {
            throw new IllegalArgumentException("Emails can't be the same.");
        }
        if (!isEmail(dto.newEmail())) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (_authRepository.existsByEmail(dto.newEmail())) {
            throw new BadRequestException("Email is already taken.");
        }

        User user = _authRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User doesn't exist."));

        if (user.getEmail().equals(dto.currentEmail())) {

            user.setEmail(dto.newEmail());
            User saved = _authRepository.save(user);

            return new UpdateResponseDto(
                    saved.getId(),
                    saved.getEmail(),
                    saved.getUsername()
            );

        } else {
            throw new ForbiddenException("Current email is incorrect.");
        }
    }

    @Override
    public UpdateResponseDto changeUsername(UpdateUsernameDto dto, UUID id) {

        if (dto.currentUsername().equals(dto.newUsername())) {
            throw new IllegalArgumentException("Usernames can't be the same.");
        }
        if (_authRepository.existsByUsername(dto.newUsername())) {
            throw new BadRequestException("Username is already taken.");
        }

        User user = _authRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User doesn't exist."));

        if (user.getUsername().equals(dto.currentUsername())) {

            user.setUsername(dto.newUsername());
            User saved = _authRepository.save(user);

            return new UpdateResponseDto(
                    saved.getId(),
                    saved.getEmail(),
                    saved.getUsername()
            );

        } else {
            throw new ForbiddenException("Current username is incorrect.");
        }
    }

    // Helper function to check email format
    private boolean isEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]+$");
    }
}