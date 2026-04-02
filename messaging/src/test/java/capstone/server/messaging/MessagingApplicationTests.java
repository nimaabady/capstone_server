package capstone.server.messaging;

import capstone.server.messaging.dto.IncomingMessage;
import capstone.server.messaging.dto.MessageAck;
import capstone.server.messaging.dto.OutgoingMessage;
import capstone.server.messaging.model.Message;
import capstone.server.messaging.model.MessageStatus;
import capstone.server.messaging.repository.MessageRepository;
import capstone.server.messaging.service.MessageServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessagingApplicationTests {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private Principal principal;

    @InjectMocks
    private MessageServiceImpl messageService;

    @Test
    void sendMessage() {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        when(principal.getName()).thenReturn(senderId.toString());

        IncomingMessage incoming = new IncomingMessage(receiverId, "Hi!");
        messageService.sendPrivateMessage(principal, incoming);

        // Verify WebSocket delivery: Spring's convertAndSendToUser prefixes the destination with "/user"
        // and appends the destination. Final path: /user/{receiverId}/queue/messages
        verify(messagingTemplate).convertAndSendToUser(
                eq(receiverId.toString()),
                eq("/queue/messages"),
                any(OutgoingMessage.class)
        );

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void acknowledgeSuccess() {
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        Message existingMsg = Message.builder()
                .id(messageId)
                .sender(senderId)
                .receiver(receiverId)
                .status(MessageStatus.SENT)
                .build();

        when(principal.getName()).thenReturn(receiverId.toString());
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(existingMsg));

        MessageAck ackDto = new MessageAck(messageId);
        messageService.acknowledge(principal, ackDto);

        assertEquals(MessageStatus.DELIVERED, existingMsg.getStatus());
        verify(messageRepository).save(existingMsg);

        // Verify the sender is notified on their /queue/delivery
        verify(messagingTemplate).convertAndSendToUser(
                eq(senderId.toString()),
                eq("/queue/delivery"),
                any(MessageAck.class)
        );
    }

    @Test
    void acknowledgeSecurityViolation() {
        UUID messageId = UUID.randomUUID();
        UUID actualReceiverId = UUID.randomUUID();
        UUID maliciousUserId = UUID.randomUUID();

        Message msg = Message.builder()
                .id(messageId)
                .receiver(actualReceiverId)
                .build();

        when(principal.getName()).thenReturn(maliciousUserId.toString());
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(msg));

        assertThrows(AccessDeniedException.class, () ->
                messageService.acknowledge(principal, new MessageAck(messageId))
        );
    }

    @Test
    void syncUndelivered() {
        UUID receiverId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        when(principal.getName()).thenReturn(receiverId.toString());

        Message m1 = Message.builder().id(UUID.randomUUID()).sender(senderId).receiver(receiverId)
                .content("Missed 1").status(MessageStatus.SENT).createdAt(Instant.now()).build();
        Message m2 = Message.builder().id(UUID.randomUUID()).sender(senderId).receiver(receiverId)
                .content("Missed 2").status(MessageStatus.SENT).createdAt(Instant.now()).build();

        when(messageRepository.findByReceiverAndStatusOrderByCreatedAt(receiverId, MessageStatus.SENT))
                .thenReturn(List.of(m1, m2));
        messageService.syncUndelivered(principal);

        // Verify messagingTemplate was called for each message
        verify(messagingTemplate, times(2)).convertAndSendToUser(
                eq(receiverId.toString()),
                eq("/queue/messages"),
                any(OutgoingMessage.class)
        );

        // Specifically check that the content of the second message was sent
        verify(messagingTemplate).convertAndSendToUser(
                eq(receiverId.toString()),
                eq("/queue/messages"),
                argThat(argument -> ((OutgoingMessage) argument).content().equals("Missed 2"))
        );
    }
}