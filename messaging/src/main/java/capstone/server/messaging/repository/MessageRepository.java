package capstone.server.messaging.repository;

import capstone.server.messaging.model.Message;
import capstone.server.messaging.model.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByReceiverAndStatusOrderByCreatedAt(
            UUID receiver,
            MessageStatus status
    );

}
