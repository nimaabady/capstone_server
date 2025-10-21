package com.capstone.server.userlogin.service;

import com.capstone.server.userlogin.dto.LoginRequest;
import com.capstone.server.userlogin.dto.LoginResponse;

public interface LoginService {
    LoginResponse loginUser(LoginRequest loginRequest);
}
