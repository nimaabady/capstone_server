package com.capstone.server.friends.dto;

import java.util.UUID;

public record FriendRequest(
        UUID id,
        UUID userId,
        UUID friendId,
        String status
) {
}
