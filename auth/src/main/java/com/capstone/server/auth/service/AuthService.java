package com.capstone.server.auth.service;

import com.capstone.server.auth.dto.LoginRequestDto;
import com.capstone.server.auth.dto.LoginResponseDto;
import com.capstone.server.auth.dto.RegisterResponseDto;
import com.capstone.server.auth.dto.RegisterRequestDto;

public interface AuthService {

   RegisterResponseDto registerUser(RegisterRequestDto dto);

   LoginResponseDto loginUser(LoginRequestDto dto);


}
