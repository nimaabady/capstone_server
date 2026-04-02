package capstone.server.messaging.config;

import capstone.server.messaging.jwt.service.JwtService;
import capstone.server.messaging.repository.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final GroupMemberRepository groupMemberRepository;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
        registry.addEndpoint("/ws-sockjs").setAllowedOriginPatterns("*").withSockJS();
        log.info("WebSocket endpoints registered at /ws and /ws-sockjs");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
        registry.enableSimpleBroker("/queue", "/topic");
        log.info("Message broker configured with prefixes /app and /user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && accessor.getCommand() != null) {
                    log.debug("Received STOMP Command: {}", accessor.getCommand());
                }

                // secure the connection if user has valid token
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        if (jwtService.isTokenValid(token)) {
                            String userId = jwtService.extractUserId(token);
                            accessor.setUser(() -> userId);
                            log.info("WebSocket Auth SUCCESS: User {} connected", userId);
                        } else {
                            log.warn("WebSocket Auth FAILED: Invalid JWT token provided");
                            throw new IllegalArgumentException("Invalid JWT");
                        }
                    } else {
                        log.warn("WebSocket Auth FAILED: Missing Authorization header");
                        throw new IllegalArgumentException("Missing JWT");
                    }
                }

                // room subscription
                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    String destination = accessor.getDestination();
                    Principal user = accessor.getUser();

                    if (destination != null && destination.startsWith("/topic/room.") && user != null) {
                        try {
                            String groupIdStr = destination.replace("/topic/room.", "");
                            UUID groupId = UUID.fromString(groupIdStr);
                            UUID userId = UUID.fromString(user.getName());

                            if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
                                log.error("AUTH DENIED: User {} attempted to subscribe to unauthorized group {}", userId, groupId);
                                throw new AccessDeniedException("Not a member of this group");
                            }
                            log.info("Subscription AUTHORIZED: User {} joined room {}", userId, groupId);
                        } catch (Exception e) {
                            log.error("Subscription ERROR: Invalid destination format or unauthorized access: {}", destination);
                            throw e;
                        }
                    }
                }
                return message;
            }
        });
    }
}