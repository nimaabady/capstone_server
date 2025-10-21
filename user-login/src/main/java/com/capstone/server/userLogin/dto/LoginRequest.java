package com.capstone.server.userLogin.dto;

import java.util.Date;


public record LoginRequest(
        String id,
        String email,
        String username,
        String password,
        String status,
        Date created_at
) {
}
