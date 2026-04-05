package capstone.server.messaging.dto;

import java.util.UUID;

public record GroupMemberInfo(
        UUID userId,
        UUID groupId
) {
}
