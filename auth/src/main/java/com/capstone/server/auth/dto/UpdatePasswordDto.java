package com.capstone.server.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePasswordDto(

        @NotBlank String currentPassword,

        @NotBlank String newPassword
) {
}
