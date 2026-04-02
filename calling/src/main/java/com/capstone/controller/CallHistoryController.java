package com.capstone.controller;



import com.capstone.model.CallRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.capstone.service.CallHistoryService;

import java.util.List;

@RestController
@RequestMapping("/api/calls")
@CrossOrigin(origins = "*")
public class CallHistoryController {

    @Autowired
    private CallHistoryService callHistoryService;

    @PostMapping("/save")
    public CallRecord saveCall(@RequestBody CallRecord record) {
        return callHistoryService.saveCall(record);
    }

    @GetMapping("/history/{userId}")
    public List<CallRecord> getHistory(@PathVariable String userId) {
        return callHistoryService.getCallHistory(userId);
    }
}