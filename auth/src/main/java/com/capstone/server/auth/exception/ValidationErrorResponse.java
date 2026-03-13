package com.capstone.server.auth.exception;

import java.time.Instant;
import java.util.Map;

public record ValidationErrorResponse(

        Instant timestamp,
        int statusCode,
        String error,
        Map<String, String> fields
) {
}