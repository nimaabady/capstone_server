package com.capstone.controller;

import com.capstone.model.CallRecord;
import com.capstone.model.SignalMessage;
import com.capstone.service.CallHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class SignalingController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private CallHistoryService callHistoryService;

    @Autowired
    private SimpUserRegistry userRegistry;

    /**
     * Routes to /user/{targetUserId}/queue/call. The target must match the callee's JWT subject (principal name)
     * exactly, and the callee must SUBSCRIBE to /user/queue/call.
     */
    private void sendCallSignalToUser(String targetUserId, SignalMessage message) {
        if (targetUserId == null || targetUserId.isBlank()) {
            System.err.println("[call-signaling] Missing targetUserId; signal not delivered.");
            return;
        }
        SimpUser user = userRegistry.getUser(targetUserId);
        if (user == null || user.getSessions().isEmpty()) {
            System.err.println(
                    "[call-signaling] No active STOMP session for userId='"
                            + targetUserId
                            + "'. "
                            + "Ensure targetUserId equals the callee JWT subject (UUID string from auth) "
                            + "and the client subscribes to destination /user/queue/call."
            );
        }
        messagingTemplate.convertAndSendToUser(targetUserId, "/queue/call", message);
    }

    @MessageMapping("/call.offer")
    public void handleOffer(@Payload SignalMessage message) {
        System.out.println("Offer from: " + message.getSenderId() + " to: " + message.getTargetUserId());
        sendCallSignalToUser(message.getTargetUserId(), message);
    }

    @MessageMapping("/call.answer")
    public void handleAnswer(@Payload SignalMessage message) {
        System.out.println("Answer from: " + message.getSenderId() + " to: " + message.getTargetUserId());
        sendCallSignalToUser(message.getTargetUserId(), message);
    }

    @MessageMapping("/call.ice")
    public void handleIceCandidate(@Payload SignalMessage message) {
        sendCallSignalToUser(message.getTargetUserId(), message);
    }

    @MessageMapping("/call.decline")
    public void handleDecline(@Payload SignalMessage message) {
        System.out.println("Call declined by: " + message.getSenderId());
        try {
            // Payload: senderId = callee who declined, targetUserId = original caller (see notify below)
            CallRecord record = new CallRecord(
                    message.getTargetUserId(),
                    message.getSenderId(),
                    "declined",
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    0L
            );
            callHistoryService.saveCall(record);
        } catch (Exception e) {
            System.err.println("Failed to save declined call: " + e.getMessage());
        }

        // Notify the caller (targetUserId is the caller)
        sendCallSignalToUser(message.getTargetUserId(), message);
    }

    @MessageMapping("/call.end")
    public void handleEnd(@Payload SignalMessage message) {
        System.out.println("Call ended by: " + message.getSenderId());
        try {
            CallRecord record = new CallRecord(
                    message.getSenderId(),
                    message.getTargetUserId(),
                    "completed",
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    0L
            );
            callHistoryService.saveCall(record);
        } catch (Exception e) {
            System.err.println("Failed to save ended call: " + e.getMessage());
        }

        sendCallSignalToUser(message.getTargetUserId(), message);
    }

    @MessageMapping("/call.error")
    public void handleError(@Payload SignalMessage message) {
        System.out.println("Call error from: " + message.getSenderId());
        try {
            CallRecord record = new CallRecord(
                    message.getSenderId(),
                    message.getTargetUserId(),
                    "failed",
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    0L
            );
            callHistoryService.saveCall(record);
        } catch (Exception e) {
            System.err.println("Failed to save failed call: " + e.getMessage());
        }

        sendCallSignalToUser(message.getTargetUserId(), message);
    }
}