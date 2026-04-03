package capstone.server.messaging.dto;

import java.util.UUID;

public record GroupMemberRequest(
        UUID groupId,
        UUID userId
) {}