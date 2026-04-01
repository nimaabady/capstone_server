package com.capstone.server.auth.dto;

import com.capstone.server.auth.model.UserStatus;

import java.util.UUID;

public record LoginResponseDto(

        UUID id,
        String token,
        String email,
        String username,
        UserStatus status
) {
}
