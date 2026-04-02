package capstone.server.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record OutgoingGroupMessage(
        UUID messageId,
        UUID groupId,
        UUID senderId,
        String content,
        Instant timestamp
) {}