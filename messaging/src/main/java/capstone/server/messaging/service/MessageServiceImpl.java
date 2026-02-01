package capstone.server.messaging.service;

import capstone.server.messaging.dto.IncomingMessage;
import capstone.server.messaging.dto.MessageAck;
import capstone.server.messaging.dto.OutgoingMessage;
import capstone.server.messaging.model.Message;
import capstone.server.messaging.model.MessageStatus;
import capstone.server.messaging.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    public void sendMessage(Principal principal, IncomingMessage dto) {

        UUID senderId = UUID.fromString(principal.getName());

        Message entity = Message.builder()
                .sender(senderId)
                .receiver(String.valueOf(dto.receiver()))
                .content(UUID.fromString(dto.content()))
                .status(MessageStatus.SENT)
                .createdAt(Instant.now())
                .build();

        messageRepository.save(entity);

        messagingTemplate.convertAndSendToUser(
                dto.receiver().toString(),
                "/queue/messages",
                new OutgoingMessage(
                        senderId,
                        dto.content(),
                        entity.getCreatedAt()
                )
        );
    }

    @Override
    public void acknowledge(Principal principal, MessageAck ack) {

        Message message = messageRepository.findById(ack.messageId())
                .orElseThrow();

        UUID receiverId = UUID.fromString(principal.getName());

        if (!message.getReceiver().equals(receiverId)) {
            throw new SecurityException("Invalid ACK");
        }

        message.setStatus(MessageStatus.DELIVERED);
        messageRepository.save(message);
    }
}

