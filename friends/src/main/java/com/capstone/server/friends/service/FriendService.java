package com.capstone.server.friends.service;

import com.capstone.server.friends.dto.FriendRequest;
import com.capstone.server.friends.dto.FriendResponse;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface FriendService {
    FriendResponse sendFriendRequest(FriendRequest friendRequest);
    ResponseEntity<?> handleFriendRequest(FriendRequest friendRequest);
    List<UUID> findAllFriends(UUID user_id);
}
