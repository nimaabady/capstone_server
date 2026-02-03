package capstone.server.messaging.controller;

import capstone.server.messaging.dto.IncomingMessage;
import capstone.server.messaging.dto.MessageAck;
import capstone.server.messaging.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @MessageMapping("/message.send")
    public void sendMessage(IncomingMessage dto, Principal principal) {
        messageService.sendMessage(principal, dto);
    }

    @MessageMapping("/message.ack")
    public void acknowledgeMessage(MessageAck ack, Principal principal) {
        messageService.acknowledge(principal, ack);
    }
}
