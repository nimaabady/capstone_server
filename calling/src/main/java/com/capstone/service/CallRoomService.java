package com.capstone.service;

import com.capstone.model.CallParticipant;
import com.capstone.model.CallRoom;
import com.capstone.repository.CallParticipantRepository;
import com.capstone.repository.CallRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CallRoomService {

    @Autowired
    private CallRoomRepository callRoomRepository;

    @Autowired
    private CallParticipantRepository callParticipantRepository;

    // Create a new group call room
    public CallRoom createRoom(String creatorId) {
        String roomId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        CallRoom room = new CallRoom(roomId, creatorId, "active", LocalDateTime.now());
        CallRoom savedRoom = callRoomRepository.save(room);

        // Add creator as first participant
        CallParticipant creator = new CallParticipant(
                creatorId, "joined", LocalDateTime.now(), savedRoom
        );
        callParticipantRepository.save(creator);

        return savedRoom;
    }

    // Join an existing room
    public CallRoom joinRoom(String roomId, String userId) {
        CallRoom room = callRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        CallParticipant participant = new CallParticipant(
                userId, "joined", LocalDateTime.now(), room
        );
        callParticipantRepository.save(participant);

        return room;
    }

    // Leave a room
    public void leaveRoom(String roomId, String userId) {
        List<CallParticipant> participants = callParticipantRepository.findByRoomRoomId(roomId);
        participants.stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .ifPresent(p -> {
                    p.setStatus("left");
                    p.setLeftAt(LocalDateTime.now());
                    callParticipantRepository.save(p);
                });

        // End room if no active participants left
        long activeCount = participants.stream()
                .filter(p -> p.getStatus().equals("joined"))
                .count();

        if (activeCount <= 1) {
            callRoomRepository.findByRoomId(roomId).ifPresent(room -> {
                room.setStatus("ended");
                room.setEndedAt(LocalDateTime.now());
                callRoomRepository.save(room);
            });
        }
    }

    // Get all participants in a room
    public List<CallParticipant> getRoomParticipants(String roomId) {
        return callParticipantRepository.findByRoomRoomId(roomId);
    }

    // Get room details
    public CallRoom getRoom(String roomId) {
        return callRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }
}
