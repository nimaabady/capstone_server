package com.capstone.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

public class JwtChannelInterceptor implements ChannelInterceptor {

    private final String jwtSecret = "2502db48edbfbe54b02d318f50e4e4cb";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            accessor.setLeaveMutable(true);
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            System.out.println("WebSocket CONNECT received, Auth header: " + authHeader);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);

                    SecretKey key = Keys.hmacShaKeyFor(
                            jwtSecret.getBytes(StandardCharsets.UTF_8)
                    );

                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

                    String userId = claims.getSubject();
                    System.out.println("WebSocket connected user: " + userId);

                    accessor.setUser(new Principal() {
                        @Override
                        public String getName() {
                            return userId;
                        }
                    });
                } catch (Exception e) {
                    System.err.println("JWT parsing failed: " + e.getMessage());
                }
            } else {
                System.err.println("No Authorization header in WebSocket CONNECT!");
            }
        }

        return message;
    }
}