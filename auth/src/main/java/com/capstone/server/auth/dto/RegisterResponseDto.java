package com.capstone.server.auth.dto;

import jakarta.validation.constraints.NotNull;

import java.sql.Date;

public record RegisterResponseDto(

        @NotNull
        String username,

        @NotNull
        String email,

        @NotNull
        Date createdAt
) {
}
