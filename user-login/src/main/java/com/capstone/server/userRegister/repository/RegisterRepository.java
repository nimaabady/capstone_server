package com.capstone.server.userRegister.repository;

import com.capstone.server.userRegister.dto.RegisterResponse;
import com.capstone.server.userRegister.model.Register;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegisterRepository extends JpaRepository<Register, Long> {

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO users (email, username, password, status, created_at)
        VALUES (:email, :username, :password, :status, NOW())
        """, nativeQuery = true)

    public RegisterResponse register(
            @Param("id") String id,
            @Param("email") String email,
            @Param("username") String username,
            @Param("password") String password,
            @Param("status") String status,
            @Param("created_at") Date created_at
    );
}
