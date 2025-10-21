package com.capstone.server.userlogin.dto;

import java.util.Date;

public record LoginRequest(
        String email,
        String username,
        String password
) {
}
