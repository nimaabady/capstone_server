package com.capstone.server.auth.dto;

import com.capstone.server.auth.model.UserStatus;

import java.sql.Date;
import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String email,
        String username,
        UserStatus status,
        Date createdAt
) {
}

