package com.capstone.server.userLogin.service;


import com.capstone.server.userLogin.dto.LoginRequest;
import com.capstone.server.userLogin.dto.LoginResponse;
import org.springframework.http.ResponseEntity;

public interface LoginService {
   LoginResponse registerUser(LoginRequest LoginRequest);
   ResponseEntity<?> loginUser(LoginRequest LoginRequest);
}
