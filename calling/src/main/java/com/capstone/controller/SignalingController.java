package com.capstone.controller;


import com.capstone.model.CallRecord;
import com.capstone.model.SignalMessage;
import com.capstone.service.CallHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class SignalingController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private CallHistoryService CallHistoryService;

    @MessageMapping("/call.offer")
    public void handleOffer(@Payload SignalMessage message, SimpMessageHeaderAccessor headerAccessor) {
        messagingTemplate.convertAndSendToUser(
                message.getTargetUserId(),
                "/queue/call",
                message
        );
    }

    @MessageMapping("/call.answer")
    public void handleAnswer(@Payload SignalMessage message) {
        messagingTemplate.convertAndSendToUser(
                message.getTargetUserId(),
                "/queue/call",
                message
        );
    }

    @MessageMapping("/call.ice")
    public void handleIceCandidate(@Payload SignalMessage message) {
        messagingTemplate.convertAndSendToUser(
                message.getTargetUserId(),
                "/queue/call",
                message
        );
    }
    @MessageMapping("/call.decline")
    public void handleDecline(@Payload SignalMessage message) {
        // Save declined call to history
        CallRecord record = new CallRecord(
                message.getSenderId(),
                message.getTargetUserId(),
                "declined",
                LocalDateTime.now(),
                LocalDateTime.now(),
                0L
        );
        CallHistoryService.saveCall(record);

        // Notify caller that receiver declined
        messagingTemplate.convertAndSendToUser(
                message.getTargetUserId(),
                "/queue/call",
                message
        );
    }
    @MessageMapping("/call.end")
    public void handleEnd(@Payload SignalMessage message) {
        // Save completed call to history
        CallRecord record = new CallRecord(
                message.getSenderId(),
                message.getTargetUserId(),
                "completed",
                LocalDateTime.now(),
                LocalDateTime.now(),
                0L
        );
        CallHistoryService.saveCall(record);

        // Notify other person call ended
        messagingTemplate.convertAndSendToUser(
                message.getTargetUserId(),
                "/queue/call",
                message
        );
    }

    @MessageMapping("/call.error")
    public void handleError(@Payload SignalMessage message) {
        // Save failed call to history
        CallRecord record = new CallRecord(
                message.getSenderId(),
                message.getTargetUserId(),
                "failed",
                LocalDateTime.now(),
                LocalDateTime.now(),
                0L
        );
        CallHistoryService.saveCall(record);

        // Notify other person of error
        messagingTemplate.convertAndSendToUser(
                message.getTargetUserId(),
                "/queue/call",
                message
        );
    }
}