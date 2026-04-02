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

    // History
    List<OutgoingMessage> getPrivateHistory(Principal principal, UUID receiverId);

    List<OutgoingGroupMessage> getGroupHistory(Principal principal, UUID groupId);

    // Group Management
    void createGroup(Principal principal, String groupName);

    void addUserToGroup(Principal admin, UUID groupId, UUID userIdToAdd);

    void leaveGroup(Principal principal, UUID groupId);

    void deleteGroup(Principal admin, UUID groupId);

    List<GroupMember> getAllUserGroups(UUID userId);
}