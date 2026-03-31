package capstone.server.gateway.jwt.exception;

import java.time.Instant;

public record ErrorResponse(

        Instant timestamp,
        int statusCode,
        String error
) {
}