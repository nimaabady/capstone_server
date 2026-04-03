package com.capstone.server.auth.dto;

import com.capstone.server.auth.model.UserStatus;
import jakarta.validation.constraints.NotNull;


public record UpdateUserStatusDto(

        @NotNull UserStatus status
) {
}
