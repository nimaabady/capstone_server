package capstone.server.messaging.controller;

import capstone.server.messaging.dto.*;
import capstone.server.messaging.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messaging")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // STOMP WebSocket Routes

    // 1-to-1 Route
    @MessageMapping("/message.send")
    public void sendPrivateMessage(IncomingMessage msg, Principal principal) {
        messageService.sendPrivateMessage(principal, msg);
    }

    // Group Route
    @MessageMapping("/group.send")
    public void sendGroupMessage(IncomingGroupMessage msg, Principal principal) {
        messageService.sendGroupMessage(principal, msg);
    }

    @MessageMapping("/message.ack")
    public void acknowledgeMessage(MessageAck ack, Principal principal) {
        messageService.acknowledge(principal, ack);
    }

    @MessageMapping("/message.sync")
    public void syncMessages(Principal principal) {
        messageService.syncUndelivered(principal);
    }

    // REST API Routes (Group Management & History)

    @GetMapping("/history/private/{userId}")
    public ResponseEntity<List<OutgoingMessage>> getPrivateHistory(Principal principal, @PathVariable UUID userId) {
        return ResponseEntity.ok(messageService.getPrivateHistory(principal, userId));
    }

    @GetMapping("/history/group/{groupId}")
    public ResponseEntity<List<OutgoingGroupMessage>> getGroupHistory(Principal principal, @PathVariable UUID groupId) {
        return ResponseEntity.ok(messageService.getGroupHistory(principal, groupId));
    }

    @PostMapping("/groups")
    public ResponseEntity<Void> createGroup(Principal principal, @RequestParam String name) {
        messageService.createGroup(principal, name);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/groups/{groupId}/members")
    public ResponseEntity<Void> addUserToGroup(Principal principal, @PathVariable UUID groupId, @RequestParam UUID userId) {
        messageService.addUserToGroup(principal, groupId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/groups/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(Principal principal, @PathVariable UUID groupId) {
        messageService.leaveGroup(principal, groupId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<Void> deleteGroup(Principal principal, @PathVariable UUID groupId) {
        messageService.deleteGroup(principal, groupId);
        return ResponseEntity.ok().build();
    }
}
