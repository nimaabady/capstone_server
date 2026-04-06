package com.capstone.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "call_participants")
public class CallParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String status;           // "joined", "left", "declined"
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private CallRoom room;

    public CallParticipant() {}

    public CallParticipant(String userId, String status, LocalDateTime joinedAt, CallRoom room) {
        this.userId = userId;
        this.status = status;
        this.joinedAt = joinedAt;
        this.room = room;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    public LocalDateTime getLeftAt() { return leftAt; }
    public void setLeftAt(LocalDateTime leftAt) { this.leftAt = leftAt; }
    public CallRoom getRoom() { return room; }
    public void setRoom(CallRoom room) { this.room = room; }
}
