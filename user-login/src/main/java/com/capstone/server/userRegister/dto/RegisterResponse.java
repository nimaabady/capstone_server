package com.capstone.server.userRegister.dto;

import java.util.Date;

public record RegisterResponse(
        String id,
        String email,
        String username,
        String password,
        String status,
        Date created_at
) {
}
