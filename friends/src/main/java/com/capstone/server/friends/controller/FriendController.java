package com.capstone.server.friends.controller;

import com.capstone.server.friends.dto.FriendRequest;
import com.capstone.server.friends.dto.FriendResponse;
import com.capstone.server.friends.service.FriendService;
import com.capstone.server.friends.service.FriendServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FriendController {

    FriendService friendService;

    @Autowired
    public FriendController(FriendService service) {this.friendService = service;}

    @PostMapping("/friendRequest")
    public ResponseEntity<?> friendRequest(@RequestBody FriendRequest friendRequest){
        return new ResponseEntity<>(this.friendService.sendFriendRequest(friendRequest), HttpStatus.OK);
    }

    @PostMapping("/handleFriendRequest")
    public ResponseEntity<?> handleFriendRequest(@RequestBody FriendRequest friendRequest){
        return this.friendService.handleFriendRequest(friendRequest);
    }

    @GetMapping("/getAllFriends/{user_id}")
    public ResponseEntity<?> getAllFriends(@PathVariable UUID user_id){
        return new ResponseEntity<>(this.friendService.findAllFriends(user_id), HttpStatus.OK);
    }
}
