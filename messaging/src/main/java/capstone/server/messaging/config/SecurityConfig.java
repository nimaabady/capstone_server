package capstone.server.messaging.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

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
                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/ws/**").permitAll()
//                        .anyRequest().authenticated()
//                );
                .authorizeHttpRequests(auth -> auth
                        .anyRequest()
                        .permitAll()
                );

        return http.build();
    }
}
