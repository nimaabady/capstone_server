package capstone.server.messaging.repository;

import capstone.server.messaging.model.Message;
import capstone.server.messaging.model.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByReceiverAndStatusOrderByCreatedAt(
            UUID receiver,
            MessageStatus status
    );

    // To fetch 1-to-1 history between two users
    @Query("SELECT m FROM Message m WHERE (m.sender = :u1 AND m.receiver = :u2) OR (m.sender = :u2 AND m.receiver = :u1) ORDER BY m.createdAt ASC")
    List<Message> findChatHistory(UUID u1, UUID u2);

}
