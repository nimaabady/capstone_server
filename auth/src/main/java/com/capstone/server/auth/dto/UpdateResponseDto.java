package com.capstone.server.auth.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateResponseDto(

        @NotNull
        UUID id,

        @NotNull
        String email,

        @NotNull
        String username

) { }
