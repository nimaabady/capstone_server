package capstone.server.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record OutgoingMessage(
        UUID sender,
        String content,
        Instant timestamp
) {
}

