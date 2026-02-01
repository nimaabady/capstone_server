package com.capstone.server.friends.dto;

import java.util.UUID;

public record FriendResponse(
        UUID id,
        UUID userId,
        UUID friendId,
        String status
) {
}
