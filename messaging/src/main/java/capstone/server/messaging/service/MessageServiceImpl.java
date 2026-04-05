package capstone.server.messaging.service;

import capstone.server.messaging.dto.*;
import capstone.server.messaging.model.*;
import capstone.server.messaging.repository.GroupChatMessageRepository;
import capstone.server.messaging.repository.GroupChatRepository;
import capstone.server.messaging.repository.GroupMemberRepository;
import capstone.server.messaging.repository.MessageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupChatMessageRepository groupChatMessageRepository;
    private final GroupChatRepository groupChatRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void sendPrivateMessage(Principal principal, IncomingMessage incoming) {
        // Check if this message is already processed by server
        if (messageRepository.existsById(incoming.messageId())) {
            log.warn("Message {} already exists. Skipping save to prevent locking failure.", incoming.messageId());
            return;
        }

        UUID senderId = UUID.fromString(principal.getName());
        log.info("Private Message: {} sending to {}", senderId, incoming.receiver());

        Message msg = Message.builder()
                .id(incoming.messageId())
                .sender(senderId)
                .receiver(incoming.receiver())
                .content(incoming.content())
                .status(MessageStatus.SENT)
                .createdAt(Instant.now())
                .build();

        try {
            Message savedMsg = messageRepository.save(msg);

            messagingTemplate.convertAndSendToUser(
                    incoming.receiver().toString(),
                    "/queue/messages",
                    new OutgoingMessage(
                            savedMsg.getId(),
                            senderId, savedMsg.getReceiver(),
                            incoming.content(),
                            savedMsg.getCreatedAt()
                    )
            );
            log.debug("Private Message {} successfully routed and saved", savedMsg.getId());
        } catch (ObjectOptimisticLockingFailureException e) {
            log.error("Optimistic lock failure for message {}. Another thread likely handled it.", incoming.messageId());
        }
    }

    @Override
    public void sendGroupMessage(Principal sender, IncomingGroupMessage incoming) {
        UUID senderId = UUID.fromString(sender.getName());
        log.info("Group Message: User {} sending to Group {}", senderId, incoming.groupId());

        if (!groupMemberRepository.existsByGroupIdAndUserId(incoming.groupId(), senderId)) {
            log.warn("BLOCKED: User {} tried to send message to Group {} without membership", senderId, incoming.groupId());
            throw new AccessDeniedException("You are not a member of this group");
        }

        // create group message
        GroupChatMessage groupMsg = GroupChatMessage.builder()
                .id(incoming.messageId())
                .sender(senderId)
                .groupId(incoming.groupId())
                .content(incoming.content())
                .status(GroupMessageStatus.SENT)
                .createdAt(Instant.now())
                .build();

        // save message to db
        GroupChatMessage savedMsg = groupChatMessageRepository.save(groupMsg);

        // send to room
        messagingTemplate.convertAndSend(
                "/topic/room." + incoming.groupId(),
                new OutgoingGroupMessage(
                        savedMsg.getId(),
                        senderId,
                        incoming.groupId(),
                        incoming.content(),
                        savedMsg.getCreatedAt()
                )
        );
        log.debug("Group Message {} broadcasted to room {}", savedMsg.getId(), incoming.groupId());
    }

    @Override
    public void acknowledge(Principal principal, MessageAck ack) {
        UUID receiverId = UUID.fromString(principal.getName());

        Message msg = messageRepository.findById(ack.messageId())
                .orElseThrow(() -> {
                    log.error("ACK FAILED: Message {} not found", ack.messageId());
                    return new IllegalArgumentException("Message not found");
                });

        if (!msg.getReceiver().equals(receiverId)) {
            log.warn("ACK DENIED: User {} tried to acknowledge message {} belonging to another user", receiverId, msg.getId());
            throw new AccessDeniedException("Invalid ACK");
        }

        if (msg.getStatus() == MessageStatus.DELIVERED) return;

        msg.setStatus(MessageStatus.DELIVERED);
        messageRepository.save(msg);

        messagingTemplate.convertAndSendToUser(
                msg.getSender().toString(),
                "/queue/delivery",
                new MessageAck(msg.getId())
        );
        log.info("Message {} acknowledged by {}", msg.getId(), receiverId);
    }

    @Override
    public void acknowledgeGroup(Principal principal, MessageAck ack) {

        GroupChatMessage msg = groupChatMessageRepository.findById(ack.messageId())
                .orElseThrow(() -> {
                    log.error("ACK FAILED: Message {} not found", ack.messageId());
                    return new IllegalArgumentException("Message not found");
                });

        if (msg.getStatus() == GroupMessageStatus.DELIVERED) return;

        msg.setStatus(GroupMessageStatus.DELIVERED);
        groupChatMessageRepository.save(msg);
        log.info("Message {} acknowledged by {}", msg.getId(), msg.getSender());
    }

    @Override
    public void syncUndelivered(Principal principal) {
        UUID receiverId = UUID.fromString(principal.getName());
        log.info("Syncing undelivered messages for user: {}", receiverId);

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
                            msg.getReceiver(),
                            msg.getContent(),
                            msg.getCreatedAt()
                    )
            );
        }
        log.info("Sync complete. Sent {} pending messages to {}", undeliveredMsg.size(), receiverId);
    }

    @Override
    public List<OutgoingGroupMessage> getGroupHistory(Principal principal, UUID groupId) {
        UUID userId = UUID.fromString(principal.getName());
        log.debug("User {} fetching history for Group {}", userId, groupId);

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            log.warn("History Access Denied: User {} not in Group {}", userId, groupId);
            throw new AccessDeniedException("Not a member");
        }

        return groupChatMessageRepository.findByGroupIdOrderByCreatedAtAsc(groupId)
                .stream()
                .map(m -> new OutgoingGroupMessage(
                        m.getId(),
                        m.getGroupId(),
                        m.getSender(),
                        m.getContent(),
                        m.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CreateGroupRequestDto createGroup(Principal principal, CreateGroupRequestDto dto) {
        UUID creatorId = UUID.fromString(principal.getName());
        log.info("Creating Group: '{}' by User {}", dto.name(), creatorId);

        GroupChat group = GroupChat.builder()
                .id(dto.groupId())
                .name(dto.name())
                .createdAt(Instant.now())
                .build();

        GroupChat savedGroup = groupChatRepository.save(group);

        GroupMember admin = GroupMember.builder()
                .group(savedGroup)
                .userId(creatorId)
                .role(GroupMemberRole.ADMIN)
                .joinedAt(Instant.now())
                .build();

        groupMemberRepository.save(admin);
        log.info("Group '{}' successfully created with ID: {}", dto.name(), savedGroup.getId());
        log.info("User {} is the admin", creatorId);
        return new CreateGroupRequestDto(dto.groupId(), dto.name());
    }

    @Override
    @Transactional
    public void addUserToGroup(Principal groupMember, UUID groupId, UUID userToAdd) {
        UUID userId = UUID.fromString(groupMember.getName());
        log.info("User {} adding User {} to Group {}", userId, userToAdd, groupId);

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            log.warn("Access Denied: User {} is not in group {}", userId, groupId);
            throw new AccessDeniedException("You must be a member to add others");
        }

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userToAdd)) {
            log.debug("User {} is already in Group {}", userToAdd, groupId);
            return;
        }

        GroupChat group = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        GroupMember newMember = GroupMember.builder()
                .group(group)
                .userId(userToAdd)
                .role(GroupMemberRole.MEMBER)
                .joinedAt(Instant.now())
                .build();

        groupMemberRepository.save(newMember);
        log.info("User {} successfully added to Group {} by {}", userToAdd, groupId, userId);
    }

    @Override
    @Transactional
    public void leaveGroup(Principal principal, UUID groupId) {
        UUID userId = UUID.fromString(principal.getName());
        log.info("User {} is leaving Group {}", userId, groupId);
        groupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);
    }

    @Override
    @Transactional
    public void deleteGroup(Principal adminPrincipal, UUID groupId) {
        UUID adminId = UUID.fromString(adminPrincipal.getName());
        log.warn("Admin {} ATTEMPTING DELETE for Group {}", adminId, groupId);

        groupMemberRepository.findByGroupId(groupId).stream()
                .filter(m -> m.getUserId().equals(adminId) && m.getRole() == GroupMemberRole.ADMIN)
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("Only admins can delete the group"));

        groupChatMessageRepository.deleteByGroupId(groupId);
        groupChatRepository.deleteGroupChatById(groupId);
        log.info("Group {} and all its messages successfully deleted", groupId);
    }

    @Override
    @Transactional
    public List<GroupInfoDto> getAllUserGroups(UUID userId) {
        log.info("Fetching group info for user {}", userId);
        // Directly returns the record list from the repo
        return groupMemberRepository.findGroupsByUserId(userId);
    }
}