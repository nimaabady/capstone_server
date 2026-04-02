package com.capstone.controller;



import com.capstone.model.CallParticipant;
import com.capstone.model.SignalMessage;
import com.capstone.service.CallRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.List;

@Controller
public class GroupSignalingController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private CallRoomService callRoomService;

    // When someone joins, notify all other participants
    @MessageMapping("/group.join")
    public void handleJoin(@Payload SignalMessage message) {
        List<CallParticipant> participants = callRoomService
                .getRoomParticipants(message.getRoomId());

        // Notify every other participant that a new user joined
        participants.stream()
                .filter(p -> !p.getUserId().equals(message.getSenderId()))
                .filter(p -> p.getStatus().equals("joined"))
                .forEach(p -> messagingTemplate.convertAndSendToUser(
                        p.getUserId(),
                        "/queue/group-call",
                        message
                ));
    }

    // Forward offer to a specific participant in the room
    @MessageMapping("/group.offer")
    public void handleOffer(@Payload SignalMessage message) {
        messagingTemplate.convertAndSendToUser(
                message.getTargetUserId(),
                "/queue/group-call",
                message
        );
    }

    // Forward answer to a specific participant
    @MessageMapping("/group.answer")
    public void handleAnswer(@Payload SignalMessage message) {
        messagingTemplate.convertAndSendToUser(
                message.getTargetUserId(),
                "/queue/group-call",
                message
        );
    }

    // Forward ICE candidates
    @MessageMapping("/group.ice")
    public void handleIce(@Payload SignalMessage message) {
        messagingTemplate.convertAndSendToUser(
                message.getTargetUserId(),
                "/queue/group-call",
                message
        );
    }

    // When someone leaves, notify all others
    @MessageMapping("/group.leave")
    public void handleLeave(@Payload SignalMessage message) {
        callRoomService.leaveRoom(message.getRoomId(), message.getSenderId());

        List<CallParticipant> participants = callRoomService
                .getRoomParticipants(message.getRoomId());

        participants.stream()
                .filter(p -> !p.getUserId().equals(message.getSenderId()))
                .filter(p -> p.getStatus().equals("joined"))
                .forEach(p -> messagingTemplate.convertAndSendToUser(
                        p.getUserId(),
                        "/queue/group-call",
                        message
                ));
    }
}
