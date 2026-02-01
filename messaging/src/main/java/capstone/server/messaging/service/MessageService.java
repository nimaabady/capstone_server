package capstone.server.messaging.service;

import capstone.server.messaging.dto.IncomingMessage;
import capstone.server.messaging.dto.MessageAck;

import java.security.Principal;

public interface MessageService {
    void sendMessage(Principal sender, IncomingMessage message);

    void acknowledge(Principal receiver, MessageAck ack);
}
