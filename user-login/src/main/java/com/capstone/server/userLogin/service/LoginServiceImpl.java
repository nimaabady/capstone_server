package com.capstone.server.userLogin.service;

import com.capstone.server.userLogin.dto.LoginRequest;
import com.capstone.server.userLogin.dto.LoginResponse;
import com.capstone.server.userLogin.model.Login;
import com.capstone.server.userLogin.repository.LoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class LoginServiceImpl implements LoginService {

    private LoginRepository regRepo;

    @Autowired
    public LoginServiceImpl(LoginRepository LoginRepository) {
        this.regRepo = LoginRepository;
    }

    @Override
    public LoginResponse registerUser(LoginRequest request) {
        Login user = Login.builder()
                .email(request.email())
                .username(request.username())
                .password(request.password())
                .status("Active")
                .build();

        Login saved = regRepo.save(user);

        return new LoginResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getUsername(),
                saved.getPassword(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }

    @Override
    public ResponseEntity<?> loginUser(LoginRequest request) {
        Optional<Login> userOpt = regRepo.findByEmail(request.email());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        Login user = userOpt.get();

        if (!user.getPassword().equals(request.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
        }

        return ResponseEntity.ok("Login Successful");
    }
}
