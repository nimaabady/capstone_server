package com.capstone.server.friends.service;

import com.capstone.server.friends.dto.FriendRequest;
import com.capstone.server.friends.dto.FriendResponse;
import com.capstone.server.friends.model.Friend;
import com.capstone.server.friends.repository.FriendRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class FriendServiceImpl implements FriendService {
    private FriendRepository friendRepository;

    @Autowired
    public FriendServiceImpl(FriendRepository FriendRepository){this.friendRepository = FriendRepository;}

    @Override
    @Transactional
    public FriendResponse sendFriendRequest(FriendRequest request) {

        if (friendRepository.existsByUsers(request.userId(), request.friendId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Friend request already exists"
            );
        }

        Friend friend = Friend.builder()
                .userId(request.userId())
                .friendId(request.friendId())
                .status("PENDING")
                .build();

        Friend saved = friendRepository.save(friend);

        return new FriendResponse(
                saved.getId(),
                saved.getUserId(),
                saved.getFriendId(),
                saved.getStatus()
        );
    }

    @Override
    public ResponseEntity<?> handleFriendRequest(FriendRequest friendRequest) {

        if ("DECLINED".equalsIgnoreCase(friendRequest.status())) {
            int rowsDeleted = friendRepository.deleteByUserIdAndFriendId(
                    friendRequest.userId(),
                    friendRequest.friendId()
            );

            if (rowsDeleted == 0) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Friend request not found to delete");
            }

            return ResponseEntity.ok("Friend request declined and deleted");
        }

        int rowsUpdated = friendRepository.editStatus(
                friendRequest.userId(),
                friendRequest.friendId(),
                friendRequest.status()
        );

        if (rowsUpdated == 0) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Friend request not found");
        }

        return ResponseEntity.ok("friend request had been: " + friendRequest.status());
    }

    @Override
    public List<UUID> findAllFriends(UUID user_id) {
        return friendRepository.findFriendIds(user_id);
    }

    @Override
    public List<UUID> findAllPendingFriendRequests(UUID userId) { return friendRepository.findPendingSenderIds(userId); }
}
