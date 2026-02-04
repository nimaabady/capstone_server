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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendMessage(Principal principal, IncomingMessage incoming) {

        UUID senderId = UUID.fromString(principal.getName());

        Message msg = Message.builder()
                .sender(senderId)
                .receiver(incoming.receiver())
                .content(incoming.content())
                .status(MessageStatus.SENT)
                .createdAt(Instant.now())
                .build();

        messageRepository.save(msg);

        messagingTemplate.convertAndSendToUser(
                incoming.receiver().toString(),
                "/queue/messages",
                new OutgoingMessage(
                        msg.getId(),
                        senderId,
                        incoming.content(),
                        msg.getCreatedAt()
                )
        );
    }

    @Override
    public void acknowledge(Principal principal, MessageAck ack) {

        UUID receiverId = UUID.fromString(principal.getName());

        Message msg = messageRepository.findById(ack.messageId())
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!msg.getReceiver().equals(receiverId)) {
            throw new AccessDeniedException("Invalid ACK");
        }

        if (msg.getStatus() == MessageStatus.DELIVERED) {
            return;
        }

        msg.setStatus(MessageStatus.DELIVERED);
        messageRepository.save(msg);

        messagingTemplate.convertAndSendToUser(
                msg.getSender().toString(),
                "/queue/delivery",
                new MessageAck(msg.getId())
        );
    }

    @Override
    public void syncUndelivered(Principal principal) {

        UUID receiverId = UUID.fromString(principal.getName());

        List<Message> undeliveredMsg = messageRepository.findByReceiverAndStatusOrderByCreatedAt(
                receiverId,
                MessageStatus.SENT
        );

        for (Message msg : undeliveredMsg) {
            messagingTemplate.convertAndSendToUser(
                    receiverId.toString(),
                    "/queue/messages",
                    new OutgoingMessage(
                            msg.getId(),
                            msg.getSender(),
                            msg.getContent(),
                            msg.getCreatedAt()
                    )
            );
        }
    }

}

