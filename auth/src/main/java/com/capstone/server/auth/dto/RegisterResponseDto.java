package com.capstone.server.auth.dto;

import java.sql.Date;
import java.util.UUID;

public record RegisterResponseDto(

        UUID id,
        String username,
        String email,
        Date createdAt
) {
}
