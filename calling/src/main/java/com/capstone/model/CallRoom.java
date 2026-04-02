package com.capstone.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "call_rooms")
public class CallRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;        // unique room code
    private String creatorId;     // who created the room
    private String status;        // "active", "ended"
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<CallParticipant> participants;

    public CallRoom() {}

    public CallRoom(String roomId, String creatorId, String status, LocalDateTime createdAt) {
        this.roomId = roomId;
        this.creatorId = creatorId;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    public List<CallParticipant> getParticipants() { return participants; }
    public void setParticipants(List<CallParticipant> participants) { this.participants = participants; }
}