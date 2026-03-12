package capstone.server.messaging.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");

        registry.addEndpoint("/ws-sockjs")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
        registry.enableSimpleBroker("/queue");
    }

    // this is for testing without users
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Read the user id header provided by the client
                    String userId = accessor.getFirstNativeHeader("user-id");
                    System.out.println("Login attempt for User ID: " + userId);

                    // if no user id create custom principal
                    if (userId != null) {
                        Principal user = new Principal() {
                            @Override
                            public String getName() {
                                return userId;
                            }
                        };
                        accessor.setUser(user);
                    }
                }
                return message;
            }
        });
    }

    //use this in prod
//    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
//        registration.interceptors(new ChannelInterceptor() {
//            @Override
//            public Message<?> preSend(Message<?> message, MessageChannel channel) {
//                StompHeaderAccessor accessor =
//                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//
//                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//                    // In Prod, look for the 'Authorization' header
//                    String bearerToken = accessor.getFirstNativeHeader("Authorization");
//
//                    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
//                        String jwt = bearerToken.substring(7);
//
//                        // 1. Validate the JWT signature and expiration
//                        // 2. Extract the 'sub' (Subject), which should be your User UUID
//                        if (jwtUtils.validateToken(jwt)) {
//                            String userId = jwtUtils.extractUserId(jwt);
//
//                            // Create a real Authentication object for Spring Security
//                            UsernamePasswordAuthenticationToken auth =
//                                    new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
//
//                            accessor.setUser(auth);
//                        } else {
//                            // Reject the connection if the token is invalid
//                            throw new MessageDeliveryException("Invalid JWT");
//                        }
//                    }
//                }
//                return message;
//            }
//        });
//    }
}