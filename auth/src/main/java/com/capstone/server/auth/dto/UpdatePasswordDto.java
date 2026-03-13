package com.capstone.server.auth.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePasswordDto(

        @NotNull
        String currentPassword,

        @NotNull
        String newPassword
) {
}
