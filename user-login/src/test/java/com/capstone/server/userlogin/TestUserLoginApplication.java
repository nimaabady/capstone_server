package com.capstone.server.userlogin;

import org.springframework.boot.SpringApplication;

public class TestUserLoginApplication {

    public static void main(String[] args) {
        SpringApplication.from(UserLoginApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
