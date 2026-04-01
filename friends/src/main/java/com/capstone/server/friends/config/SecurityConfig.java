package com.capstone.server.friends.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
//@Import({
//        capstone.server.security.config.JwtAuthenticationFilter.class,
//        capstone.server.security.config.JwtAuthenticationEntryPoint.class,
//        capstone.server.security.service.JwtService.class,
//        capstone.server.security.config.JacksonConfig.class
//})
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // disable CSRF for Postman/testing
                .authorizeHttpRequests(auth -> auth
                        // Paths moved under /api/friends (see FriendController). JWT is enforced at the gateway for clients.
                        .requestMatchers("/api/friends/**")
                        .permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}