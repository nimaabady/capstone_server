package capstone.server.messaging.service;

import capstone.server.messaging.dto.*;
import capstone.server.messaging.model.GroupMember;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

public interface MessageService {
    // Messaging
    void sendPrivateMessage(Principal sender, IncomingMessage msg);

    void sendGroupMessage(Principal sender, IncomingGroupMessage msg);

    void acknowledge(Principal receiver, MessageAck ack);

    void acknowledgeGroup(Principal receiver, MessageAck ack);


    void syncUndelivered(Principal principal);

    List<OutgoingMessage> getPrivateHistory(Principal principal, UUID otherUserId);

    // History
    List<OutgoingGroupMessage> getGroupHistory(Principal principal, UUID groupId);

    // Group Management
    CreateGroupRequestDto createGroup(Principal principal, CreateGroupRequestDto dto);

    void addUserToGroup(Principal admin, UUID groupId, UUID userIdToAdd);

    void leaveGroup(Principal principal, UUID groupId);

    void deleteGroup(Principal admin, UUID groupId);

    List<GroupInfoDto> getAllUserGroups(UUID userId);
}