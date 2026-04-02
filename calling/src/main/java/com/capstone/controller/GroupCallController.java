package com.capstone.controller;


import com.capstone.model.CallParticipant;
import com.capstone.model.CallRoom;
import com.capstone.service.CallRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/group-calls")
@CrossOrigin(origins = "*")
public class GroupCallController {

    @Autowired
    private CallRoomService callRoomService;

    // Create a new group call room
    @PostMapping("/create/{creatorId}")
    public CallRoom createRoom(@PathVariable String creatorId) {
        return callRoomService.createRoom(creatorId);
    }

    // Join a room
    @PostMapping("/join/{roomId}/{userId}")
    public CallRoom joinRoom(@PathVariable String roomId, @PathVariable String userId) {
        return callRoomService.joinRoom(roomId, userId);
    }

    // Leave a room
    @PostMapping("/leave/{roomId}/{userId}")
    public void leaveRoom(@PathVariable String roomId, @PathVariable String userId) {
        callRoomService.leaveRoom(roomId, userId);
    }

    // Get room participants
    @GetMapping("/participants/{roomId}")
    public List<CallParticipant> getParticipants(@PathVariable String roomId) {
        return callRoomService.getRoomParticipants(roomId);
    }

    // Get room details
    @GetMapping("/room/{roomId}")
    public CallRoom getRoom(@PathVariable String roomId) {
        return callRoomService.getRoom(roomId);
    }
}
