package com.capstone.server.userLogin.repository;

import com.capstone.server.userLogin.dto.LoginResponse;
import com.capstone.server.userLogin.model.Login;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface LoginRepository extends JpaRepository<Login, Long> {
    Optional<Login> findByEmail(String email);

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO users (email, username, password, status, created_at)
        VALUES (:email, :username, :password, :status, NOW())
        """, nativeQuery = true)

    public LoginResponse register(
            @Param("id") String id,
            @Param("email") String email,
            @Param("username") String username,
            @Param("password") String password,
            @Param("status") String status,
            @Param("created_at") Date created_at
    );

    @Query(value = """
        SELECT * FROM users
        WHERE email = :email
        AND password = :password
        """, nativeQuery = true)
    public LoginResponse login(
            @Param("email") String email,
            @Param("username") String username,
            @Param("password") String password
    );
}
