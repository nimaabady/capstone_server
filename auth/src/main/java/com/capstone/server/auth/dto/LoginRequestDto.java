package com.capstone.server.auth.dto;

import jakarta.validation.constraints.NotNull;

public record LoginRequestDto(

        @NotNull
        String usernameOrEmail,

        @NotNull
        String password
) {
}
