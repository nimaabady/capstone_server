package com.capstone.server.userRegister.controller;

import com.capstone.server.userRegister.dto.RegisterRequest;
import com.capstone.server.userRegister.service.RegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
public class RegisterController {


    RegisterService regService;

    @Autowired
    public RegisterController(RegisterService RegisterService) {
        this.regService = RegisterService;
    }

    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@RequestBody RegisterRequest registerRequest) {
        return new ResponseEntity<>(this.regService.registerUser(registerRequest), HttpStatus.OK);
    }
}
