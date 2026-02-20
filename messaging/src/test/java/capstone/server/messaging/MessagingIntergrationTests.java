package capstone.server.messaging;

import capstone.server.messaging.dto.IncomingMessage;
import capstone.server.messaging.dto.MessageAck;
import capstone.server.messaging.dto.OutgoingMessage;
import capstone.server.messaging.model.Message;
import capstone.server.messaging.model.MessageStatus;
import capstone.server.messaging.repository.MessageRepository;
import capstone.server.messaging.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
public class MessagingIntergrationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("messages_test")
            .withUsername("test_admin")
            .withPassword("test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private MessageService messageService;
    @Autowired
    private MessageRepository messageRepository;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;
    @MockitoBean
    private Principal principal;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
    }

    @Test
    void testSendMessage() {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        String content = "Hello from Testcontainers";
        when(principal.getName()).thenReturn(senderId.toString());

        messageService.sendMessage(principal, new IncomingMessage(receiverId, content));

        var messages = messageRepository.findByReceiverAndStatusOrderByCreatedAt(receiverId, MessageStatus.SENT);
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getContent()).isEqualTo(content);
        assertThat(messages.get(0).getSender()).isEqualTo(senderId);
    }

    @Test
    void testAcknowledge() {
        UUID receiverId = UUID.randomUUID();
        when(principal.getName()).thenReturn(receiverId.toString());

        Message msg = messageRepository.save(Message.builder()
                .sender(UUID.randomUUID())
                .receiver(receiverId)
                .content("Persistent check")
                .status(MessageStatus.SENT)
                .createdAt(Instant.now())
                .build());

        messageService.acknowledge(principal, new MessageAck(msg.getId()));

        Message updated = messageRepository.findById(msg.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(MessageStatus.DELIVERED);

        // Verify the WebSocket notification was triggered for the sender
        verify(messagingTemplate).convertAndSendToUser(
                eq(updated.getSender().toString()),
                eq("/queue/delivery"),
                any(MessageAck.class)
        );
    }

    @Test
    void testSync() {
        UUID receiverId = UUID.randomUUID();
        when(principal.getName()).thenReturn(receiverId.toString());

        // One message that should be found
        messageRepository.save(Message.builder()
                .sender(UUID.randomUUID()).receiver(receiverId).content("I am undelivered")
                .status(MessageStatus.SENT).createdAt(Instant.now()).build());

        // One message that should be ignored
        messageRepository.save(Message.builder()
                .sender(UUID.randomUUID()).receiver(receiverId).content("I am already delivered")
                .status(MessageStatus.DELIVERED).createdAt(Instant.now().minusSeconds(10)).build());

        messageService.syncUndelivered(principal);

        // The receiver should only get the 1 'SENT' message via WebSocket
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq(receiverId.toString()),
                eq("/queue/messages"),
                argThat(arg -> ((OutgoingMessage) arg).content().equals("I am undelivered"))
        );
    }
}