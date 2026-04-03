package capstone.server.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record OutgoingGroupMessage(
        UUID messageId,
        UUID senderId,
        UUID groupId,
        String content,
        Instant timestamp
) {}