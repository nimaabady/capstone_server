package capstone.server.messaging.dto;

import java.util.UUID;

public record MessageAck(
        UUID messageId
) {
}
