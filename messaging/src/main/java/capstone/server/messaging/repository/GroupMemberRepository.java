package capstone.server.messaging.repository;

import capstone.server.messaging.dto.GroupInfoDto;
import capstone.server.messaging.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    // checks user is a part of existent group
    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);

    // return list of dto of groups user is a part of
    @Query("SELECT new capstone.server.messaging.dto.GroupInfoDto(gm.group.id, gm.group.name) " +
            "FROM GroupMember gm WHERE gm.userId = :userId")
    List<GroupInfoDto> findGroupsByUserId(@Param("userId") UUID userId);

    // Get all members of a group
    List<GroupMember> findByGroupId(UUID groupId);

    // To remove a user from a group
    @Modifying
    @Query("DELETE FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.userId = :userId")
    void deleteByGroupIdAndUserId(UUID groupId, UUID userId);}
