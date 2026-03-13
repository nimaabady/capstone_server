package com.capstone.server.auth.dto;

import com.capstone.server.auth.model.UserStatus;
import jakarta.validation.constraints.NotNull;

public record LoginResponseDto(

        @NotNull
        String token,

        @NotNull
        String email,

        @NotNull
        String username,

        @NotNull
        UserStatus status
) {
}
