package com.capstone.repository;

import com.capstone.model.CallRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CallRoomRepository extends JpaRepository<CallRoom, Long> {
    Optional<CallRoom> findByRoomId(String roomId);
    List<CallRoom> findByCreatorId(String creatorId);
    List<CallRoom> findByStatus(String status);
}
