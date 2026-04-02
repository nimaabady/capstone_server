package com.capstone.service;

import com.capstone.model.CallRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.capstone.repository.CallRecordRepository;

import java.util.List;

@Service
public class CallHistoryService {

    @Autowired
    private CallRecordRepository callRecordRepository;

    public CallRecord saveCall(CallRecord record) {
        return callRecordRepository.save(record);
    }

    public List<CallRecord> getCallHistory(String userId) {
        return callRecordRepository
                .findByCallerIdOrReceiverIdOrderByStartTimeDesc(userId, userId);
    }
}