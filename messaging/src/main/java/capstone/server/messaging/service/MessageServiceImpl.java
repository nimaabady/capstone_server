package capstone.server.messaging.service;

import capstone.server.messaging.dto.IncomingMessage;
import capstone.server.messaging.dto.MessageAck;
import capstone.server.messaging.dto.OutgoingMessage;
import capstone.server.messaging.model.Message;
import capstone.server.messaging.model.MessageStatus;
import capstone.server.messaging.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendMessage(Principal principal, IncomingMessage incoming) {

        UUID senderId = UUID.fromString(principal.getName());

        Message entity = Message.builder()
                .sender(senderId)
                .receiver(UUID.fromString(String.valueOf(incoming.receiver())))
                .content(String.valueOf(incoming.content()))
                .status(MessageStatus.SENT)
                .createdAt(Instant.now())
                .build();

        messageRepository.save(entity);

        messagingTemplate.convertAndSendToUser(
                incoming.receiver().toString(),
                "/queue/messages",
                new OutgoingMessage(
                        senderId,
                        incoming.content(),
                        entity.getCreatedAt()
                )
        );
    }

    @Override
    public void acknowledge(Principal principal, MessageAck ack) {

        UUID receiverId = UUID.fromString(principal.getName());

        Message message = messageRepository.findById(ack.messageId())
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!message.getReceiver().equals(receiverId)) {
            throw new AccessDeniedException("Invalid ACK");
        }

        if (message.getStatus() == MessageStatus.DELIVERED) {
            return;
        }

        message.setStatus(MessageStatus.DELIVERED);
        messageRepository.save(message);
    }

}

