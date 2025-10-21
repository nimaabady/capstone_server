package com.capstone.server.userRegister.service;


import com.capstone.server.userRegister.dto.RegisterRequest;
import com.capstone.server.userRegister.dto.RegisterResponse;

public interface RegisterService {
   RegisterResponse registerUser(RegisterRequest registerRequest);
}
