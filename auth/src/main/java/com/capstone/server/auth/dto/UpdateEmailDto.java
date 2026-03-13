package com.capstone.server.auth.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateEmailDto(

        @NotNull
        String currentEmail,

        @NotNull
        String newEmail
) {
}
