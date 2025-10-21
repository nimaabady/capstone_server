package com.capstone.server.userlogin.dto;

import java.util.Date;

public record LoginResponse(
        String email,
        String username,
        String password
) {
}
