package capstone.server.messaging.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "sender", nullable = false)
    private UUID sender;

    @Column(name = "receiver", nullable = false)
    private UUID receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status;
}

