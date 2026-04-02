package capstone.server.messaging.repository;

import capstone.server.messaging.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    // checks user is a part of existent group
    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);

    // return list of groups user is a part of
    List<GroupMember> findByUserId(UUID userId);

    // Get all members of a group
    List<GroupMember> findByGroupId(UUID groupId);

    // To remove a user from a group
    void deleteByGroupIdAndUserId(UUID groupId, UUID userId);
}
