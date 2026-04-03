package capstone.server.messaging.dto;

import java.util.UUID;

public record IncomingGroupMessage(
        UUID messageId,
        UUID groupId,
        String content
) {}
