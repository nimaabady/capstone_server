package com.capstone.server.auth.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUsernameDto(

        @NotNull
        String currentUsername,

        @NotNull
        String newUsername
) {
}
