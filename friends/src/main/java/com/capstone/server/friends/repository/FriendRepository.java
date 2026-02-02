package com.capstone.server.friends.repository;

import com.capstone.server.friends.dto.FriendResponse;
import com.capstone.server.friends.model.Friend;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FriendRepository extends JpaRepository<Friend, UUID> {

    @Modifying
    @Transactional
    @Query("UPDATE Friend f SET f.status = :status WHERE f.userId = :userId AND f.friendId = :friendId")
    int editStatus(@Param("userId") UUID userId,
                   @Param("friendId") UUID friendId,
                   @Param("status") String status);

    @Modifying
    @Transactional
    @Query("DELETE FROM Friend f WHERE f.userId = :userId AND f.friendId = :friendId")
    int deleteByUserIdAndFriendId(@Param("userId") UUID userId,
                                  @Param("friendId") UUID friendId);

    @Query(value = """
    SELECT EXISTS (
        SELECT 1 FROM friends
        WHERE (user_id = :u1 AND friend_id = :u2)
           OR (user_id = :u2 AND friend_id = :u1)
    )
    """, nativeQuery = true)
    boolean existsByUsers(UUID u1, UUID u2);

    @Query(value = """
    SELECT
      CASE
        WHEN user_id = :userId THEN friend_id
        ELSE user_id
      END AS friend_id
    FROM friends
    WHERE status = 'ACCEPTED'
      AND (user_id = :userId OR friend_id = :userId)
    """, nativeQuery = true)
    List<UUID> findFriendIds(@Param("userId") UUID userId);
}
