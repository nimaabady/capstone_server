package com.capstone.server.auth.service;

import com.capstone.server.auth.dto.LoginRequestDto;
import com.capstone.server.auth.dto.LoginResponseDto;
import com.capstone.server.auth.dto.RegisterRequestDto;
import com.capstone.server.auth.dto.RegisterResponseDto;
import com.capstone.server.auth.exception.AuthenticationException;
import com.capstone.server.auth.exception.BadRequestException;
import com.capstone.server.auth.exception.NotFoundException;
import com.capstone.server.auth.model.User;
import com.capstone.server.auth.model.UserStatus;
import com.capstone.server.auth.repository.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthRepository _authRepository;
    private final PasswordEncoder _passwordEncoder;
    private final JwtService _jwtService;

    @Autowired
    public AuthServiceImpl(AuthRepository authRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {

        _authRepository = authRepository;
        _passwordEncoder = passwordEncoder;
        _jwtService = jwtService;
    }

    @Override
    public RegisterResponseDto registerUser(RegisterRequestDto request) {

        if (request.username() == null || request.email() == null || request.password() == null) {
            throw new BadRequestException("All fields are required.");
        }

        if (!isEmail(request.email())){
            throw new BadRequestException("Invalid email address.");
        }

        String hashedPassword = _passwordEncoder.encode(request.password());

        User user = User.builder()
                .email(request.email())
                .username(request.username())
                .hashedPassword(hashedPassword)
                .status(UserStatus.Offline)
                .build();

        User saved = _authRepository.save(user);

        return new RegisterResponseDto(
                saved.getUsername(),
                saved.getEmail(),
                new java.sql.Date(saved.getCreatedAt().getTime())
        );
    }

    @Override
    public LoginResponseDto loginUser(LoginRequestDto request) {

        if (request.usernameOrEmail() == null || request.password() == null) {
            throw new BadRequestException("All fields are required!");
        }

        User user;

        if (isEmail(request.usernameOrEmail())) {
            user = _authRepository.findByEmail(request.usernameOrEmail());
        } else {
            user = _authRepository.findByUsername(request.usernameOrEmail());
        }

        if (user == null) {
            throw new NotFoundException("User Doesn't Exist!");
        }

        if (!_passwordEncoder.matches(request.password(), user.getHashedPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }

        user.setStatus(UserStatus.Online);
        _authRepository.save(user);

        String token = _jwtService.generateToken(user);

        return new LoginResponseDto(
                token,
                user.getEmail(),
                user.getUsername(),
                user.getStatus()
        );
    }

    // Helper function to check email format
    private boolean isEmail(String email) {
            return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+.[A-Za-z]+$");
        }
    }