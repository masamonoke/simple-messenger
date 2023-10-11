package com.masamonoke.simplemessenger.api.chat;

import com.masamonoke.simplemessenger.entities.message.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/message") // app/message
    @SendTo("/topic/public")
    ChatMessage receivePublicMessage(@Payload ChatMessage message) {
        return message;
    }

    @MessageMapping("/private_message")
    ChatMessage receivePrivateMessage(@Payload ChatMessage message) {
        messagingTemplate.convertAndSendToUser(message.getReceiverName(), "/private", message); // user/<username>/private
        return message;
    }

}
