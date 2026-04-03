package capstone.server.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record OutgoingMessage(
        UUID messageId,
        UUID senderId,
        UUID receiverId,
        String content,
        Instant timestamp
) {
}

