package com.capstone.server.userLogin.dto;

import java.util.Date;
import java.util.UUID;

public record LoginResponse(
        UUID id,
        String email,
        String username,
        String password,
        String status,
        Date created_at
) {
}
