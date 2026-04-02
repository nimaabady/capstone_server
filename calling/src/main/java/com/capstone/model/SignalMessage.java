package com.capstone.model;

public class SignalMessage {
    private String type;          // "offer", "answer", "ice-candidate", "hangup", "decline", "end", "error"
    private String senderId;
    private String targetUserId;
    private String sdp;
    private String candidate;
    private String errorMessage;
    private String roomId;

    public SignalMessage() {}

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getTargetUserId() { return targetUserId; }
    public void setTargetUserId(String targetUserId) { this.targetUserId = targetUserId; }
    public String getSdp() { return sdp; }
    public void setSdp(String sdp) { this.sdp = sdp; }
    public String getCandidate() { return candidate; }
    public void setCandidate(String candidate) { this.candidate = candidate; }
    public String getErrorMessage() { return errorMessage; }        // ADD
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
}