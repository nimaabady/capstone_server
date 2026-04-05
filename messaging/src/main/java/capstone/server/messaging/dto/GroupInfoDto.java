package capstone.server.messaging.dto;

import java.util.UUID;

public record GroupInfoDto (
        UUID groupId,
        String groupName
) {
}