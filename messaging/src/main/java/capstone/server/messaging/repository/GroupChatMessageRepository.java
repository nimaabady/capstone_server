package capstone.server.messaging.repository;

import capstone.server.messaging.model.GroupChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupChatMessageRepository extends JpaRepository<GroupChatMessage, UUID> {

    // get group chat history
    List<GroupChatMessage> findByGroupIdOrderByCreatedAtAsc(UUID groupId);

    // delete all messages if group chat is deleted
    void deleteByGroupId(UUID groupId);
}