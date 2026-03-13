package com.capstone.server.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateEmailDto(

        @NotBlank @Email String currentEmail,

        @NotBlank @Email String newEmail
) {
}
