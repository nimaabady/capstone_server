package com.capstone.server.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUsernameDto(

        @NotBlank String currentUsername,

        @NotBlank String newUsername
) {
}
