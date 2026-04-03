package capstone.server.messaging.dto;

import java.util.UUID;

public record IncomingMessage(
        UUID messageId,
        UUID receiver,
        String content
) {
}
