package com.capstone.server.userLogin.controller;

import com.capstone.server.userLogin.dto.LoginRequest;
import com.capstone.server.userLogin.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {


    LoginService regService;

    @Autowired
    public LoginController(LoginService LoginService) {
        this.regService = LoginService;
    }

    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@RequestBody LoginRequest LoginRequest) {
        return new ResponseEntity<>(this.regService.registerUser(LoginRequest), HttpStatus.OK);
    }
    
    @PostMapping("/loginUser")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest LoginRequest){
        return new ResponseEntity<>(this.regService.loginUser(LoginRequest), HttpStatus.OK);
    }
}
