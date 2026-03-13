package com.capstone.server.auth.dto;

import com.capstone.server.auth.model.UserStatus;

public record LoginResponseDto(

        String token,
        String email,
        String username,
        UserStatus status
) {
}
