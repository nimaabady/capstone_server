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

    /**
     * Processes an incoming message, routes it to the intended receiver via WebSockets,
     * then saves it to the database.
     *
     * @param principal The authenticated user sending the message.
     * @param incoming  DTO containing the receiver ID and message content.
     */
    // Tested successfully
    @Override
    public void sendMessage(Principal principal, IncomingMessage incoming) {

        // Extract sender identity from the authenticated principal
        UUID senderId = UUID.fromString(principal.getName());

        // Build the message
        Message msg = Message.builder()
                .sender(senderId)
                .receiver(incoming.receiver())
                .content(incoming.content())
                .status(MessageStatus.SENT)
                .createdAt(Instant.now())
                .build();

        Message savedMsg = messageRepository.save(msg);

        // Push to the receiver's message queue
        messagingTemplate.convertAndSendToUser(
                incoming.receiver().toString(),
                "/queue/messages",
                new OutgoingMessage(
                        savedMsg.getId(),
                        senderId,
                        incoming.content(),
                        savedMsg.getCreatedAt()
                )
        );


    }

    /**
     * Updates message status to DELIVERED once the recipient acknowledges receipt.
     * Notifies the original sender that their message was received.
     *
     * @param principal The user acknowledging the message (the receiver).
     * @param ack       DTO containing the specific message ID being acknowledged.
     */
    // Tested successfully
    @Override
    public void acknowledge(Principal principal, MessageAck ack) {

        UUID receiverId = UUID.fromString(principal.getName());

        // Retrieve message and validate
        Message msg = messageRepository.findById(ack.messageId())
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        // this is for security, only the receiver can acknowledge the msg
        if (!msg.getReceiver().equals(receiverId)) {
            throw new AccessDeniedException("Invalid ACK");
        }

        // If msg already delivered return
        if (msg.getStatus() == MessageStatus.DELIVERED) {
            return;
        }

        // Update msg status and save change
        msg.setStatus(MessageStatus.DELIVERED);
        messageRepository.save(msg);

        // Notify the sender that the message was successfully delivered
        messagingTemplate.convertAndSendToUser(
                msg.getSender().toString(),
                "/queue/delivery",
                new MessageAck(msg.getId())
        );
    }

    /**
     * Fetches and resends all messages with 'SENT' status that haven't been acknowledged.
     * mainly for clients reconnecting after being offline.
     *
     * @param principal The authenticated user requesting their undelivered messages.
     */
    // TODO: Test
    @Override
    public void syncUndelivered(Principal principal) {
        UUID receiverId = UUID.fromString(principal.getName());

        // Fetch pending messages in chronological order
        List<Message> undeliveredMsg = messageRepository.findByReceiverAndStatusOrderByCreatedAt(
                receiverId,
                MessageStatus.SENT
        );

        // Re-push each message to the user's message queue
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

