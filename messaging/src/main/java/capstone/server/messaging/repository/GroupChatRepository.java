package capstone.server.messaging.repository;

import capstone.server.messaging.model.GroupChat;
import capstone.server.messaging.model.GroupChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupChatRepository extends JpaRepository<GroupChat, UUID> {

    // finds the group chat by name (idk if it will be used)
    List<GroupChat> findByNameContainingIgnoreCase(String name);

    // checks if room name is taken
    boolean existsByName(String name);

    // delete group
    void deleteGroupChatById(UUID id);
}
