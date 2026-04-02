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
    public void sendPrivateMessage(Principal principal, IncomingMessage incoming) {
        UUID senderId = UUID.fromString(principal.getName());
        log.info("Private Message: {} sending to {}", senderId, incoming.receiver());

        Message msg = Message.builder()
                .sender(senderId)
                .receiver(incoming.receiver())
                .content(incoming.content())
                .status(MessageStatus.SENT)
                .createdAt(Instant.now())
                .build();

        Message savedMsg = messageRepository.save(msg);

        messagingTemplate.convertAndSendToUser(
                incoming.receiver().toString(),
                "/queue/messages",
                new OutgoingMessage(savedMsg.getId(), senderId, incoming.content(), savedMsg.getCreatedAt())
        );
        log.debug("Private Message {} successfully routed and saved", savedMsg.getId());
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
                .sender(senderId)
                .groupId(incoming.groupId())
                .content(incoming.content())
                .status(GroupMessageStatus.SENT)
                .createdAt(Instant.now())
                .build();

        // save message to db
        GroupChatMessage savedMsg = groupChatMessageRepository.save(groupMsg);

        // give ack to sender
        messagingTemplate.convertAndSendToUser(
                senderId.toString(),
                "/queue/delivery",
                new MessageAck(savedMsg.getId())
        );

        // send to room
        messagingTemplate.convertAndSend(
                "/topic/room." + incoming.groupId(),
                new OutgoingGroupMessage(savedMsg.getId(), incoming.groupId(), senderId, incoming.content(), savedMsg.getCreatedAt())
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

        messagingTemplate.convertAndSendToUser(
                msg.getSender().toString(),
                "/queue/delivery",
                new MessageAck(msg.getId())
        );
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
                    new OutgoingMessage(msg.getId(), msg.getSender(), msg.getContent(), msg.getCreatedAt())
            );
        }
        log.info("Sync complete. Sent {} pending messages to {}", undeliveredMsg.size(), receiverId);
    }

    @Override
    public List<OutgoingMessage> getPrivateHistory(Principal principal, UUID otherUserId) {
        UUID currentUserId = UUID.fromString(principal.getName());
        log.debug("Fetching private chat history between {} and {}", currentUserId, otherUserId);

        return messageRepository.findChatHistory(currentUserId, otherUserId)
                .stream()
                .map(m -> new OutgoingMessage(m.getId(), m.getSender(), m.getContent(), m.getCreatedAt()))
                .collect(Collectors.toList());
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
                .map(m -> new OutgoingGroupMessage(m.getId(), m.getGroupId(), m.getSender(), m.getContent(), m.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createGroup(Principal principal, String groupName) {
        UUID creatorId = UUID.fromString(principal.getName());
        log.info("Creating Group: '{}' by User {}", groupName, creatorId);

        GroupChat group = GroupChat.builder().name(groupName).createdAt(Instant.now()).build();
        GroupChat savedGroup = groupChatRepository.save(group);

        GroupMember admin = GroupMember.builder()
                .group(savedGroup)
                .userId(creatorId)
                .role(GroupMemberRole.ADMIN)
                .joinedAt(Instant.now())
                .build();

        groupMemberRepository.save(admin);
        log.info("Group '{}' successfully created with ID: {}", groupName, savedGroup.getId());
    }

    @Override
    @Transactional
    public void addUserToGroup(Principal adminPrincipal, UUID groupId, UUID userIdToAdd) {
        UUID adminId = UUID.fromString(adminPrincipal.getName());
        log.info("Admin {} adding User {} to Group {}", adminId, userIdToAdd, groupId);

        groupMemberRepository.findByGroupId(groupId).stream()
                .filter(m -> m.getUserId().equals(adminId) && m.getRole() == GroupMemberRole.ADMIN)
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("ADMIN DENIED: User {} attempted to add member to Group {} without Admin role", adminId, groupId);
                    return new AccessDeniedException("Only admins can add members");
                });

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userIdToAdd)) {
            log.debug("User {} is already in Group {}", userIdToAdd, groupId);
            return;
        }

        GroupChat group = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        GroupMember newMember = GroupMember.builder()
                .group(group)
                .userId(userIdToAdd)
                .role(GroupMemberRole.MEMBER)
                .joinedAt(Instant.now())
                .build();

        groupMemberRepository.save(newMember);
        log.info("User {} successfully added to Group {}", userIdToAdd, groupId);
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
    public List<GroupMember> getAllUserGroups(UUID userId){
        log.info("user {} is a member of these groups", userId);
        List<GroupMember> groupList = groupMemberRepository.findByUserId(userId);
        for (GroupMember g: groupList) {
            log.info("Group {}", g.getGroup());
        }
        return groupList;
    }
}