package com.capstone.repository;

import com.capstone.model.CallParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CallParticipantRepository extends JpaRepository<CallParticipant, Long> {
    List<CallParticipant> findByRoomRoomId(String roomId);
    List<CallParticipant> findByUserId(String userId);
}
