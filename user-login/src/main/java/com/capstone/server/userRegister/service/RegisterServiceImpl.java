package com.capstone.server.userRegister.service;

import com.capstone.server.userRegister.dto.RegisterRequest;
import com.capstone.server.userRegister.dto.RegisterResponse;
import com.capstone.server.userRegister.model.Register;
import com.capstone.server.userRegister.repository.RegisterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RegisterServiceImpl implements RegisterService {

    private RegisterRepository regRepo;

    @Autowired
    public void UserServiceImpl(RegisterRepository registerRepository) {
        this.regRepo = registerRepository;
    }

    @Override
    public RegisterResponse registerUser(RegisterRequest request) {
        Register user = Register.builder()
                .email(request.email())
                .username(request.username())
                .password(request.password())
                .status(request.status())
                .createdAt(new Date())
                .build();

        Register saved = regRepo.save(user);

        return new RegisterResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getUsername(),
                saved.getPassword(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }
}
