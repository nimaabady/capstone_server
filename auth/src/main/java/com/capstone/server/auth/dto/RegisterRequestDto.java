package com.capstone.server.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record RegisterRequestDto(

        @NotNull
        String username,

        @NotNull
        @Email
        String email,

        @NotNull
        String password
) {
}
