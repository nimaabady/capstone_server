package com.capstone.model;



import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "call_records")
public class CallRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String callerId;
    private String receiverId;
    private String status;        // "completed", "declined", "missed", "failed"
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private String callType; // in seconds

    public CallRecord() {}

    public CallRecord(String callerId, String receiverId, String status,
                      LocalDateTime startTime, LocalDateTime endTime, Long duration) {
        this.callerId = callerId;
        this.receiverId = receiverId;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getCallerId() { return callerId; }
    public void setCallerId(String callerId) { this.callerId = callerId; }
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }
    public String getCallType() { return callType; }
    public void setCallType(String callType) { this.callType = callType; }
}
