package com.capstone.server.auth.exception;

import java.time.Instant;

public record ErrorResponse(

        Instant timestamp,
        int statusCode,
        String error
) {
}