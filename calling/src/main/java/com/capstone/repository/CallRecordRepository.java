package com.capstone.repository;

import com.capstone.model.CallRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CallRecordRepository extends JpaRepository<CallRecord, Long> {
    List<CallRecord> findByCallerIdOrReceiverIdOrderByStartTimeDesc(
            String callerId, String receiverId
    );
}
