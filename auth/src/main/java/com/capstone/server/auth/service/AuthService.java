package com.capstone.server.auth.service;

import com.capstone.server.auth.dto.*;

import java.util.UUID;

public interface AuthService {

   RegisterResponseDto registerUser(RegisterRequestDto dto);

   LoginResponseDto loginUser(LoginRequestDto dto);

   UpdateUserStatusDto updateStatus(UpdateUserStatusDto dto, UUID id);

   UserResponseDto getUserById(UUID id);

   UserIdResponseDto getUserIdByUsername(String username);

   void deleteUser(UUID id);

   void logout(UUID id);

   void changePassword(UpdatePasswordDto dto, UUID id);

   UpdateResponseDto changeEmail(UpdateEmailDto dto, UUID id);

   UpdateResponseDto changeUsername(UpdateUsernameDto dto, UUID id);

}
