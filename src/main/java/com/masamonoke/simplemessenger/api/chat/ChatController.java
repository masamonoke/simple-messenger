package com.masamonoke.simplemessenger.api.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.masamonoke.simplemessenger.entities.message.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static com.masamonoke.simplemessenger.api.Utils.decodeToken;
import static com.masamonoke.simplemessenger.api.Utils.getTokenFromHeader;

@Controller
@RequiredArgsConstructor
public class ChatController {
    //private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/message") // app/message
    @SendTo("/topic/public")
    ChatMessage receivePublicMessage(@Payload ChatMessage message) {
        return message;
    }

    @MessageMapping("/private_message")
    ChatMessage receivePrivateMessage(@Payload ChatMessage message) {
        //messagingTemplate.convertAndSendToUser(message.getReceiverName(), "/private", message); // user/<username>/private
        return chatService.receivePrivateMessage(message);
    }

    @GetMapping("/api/v1/message")
    ResponseEntity<List<ChatMessage>> getCurrentUserMessages(
            @RequestParam("receiver_username") String receiver, @RequestHeader("Authorization") String header) throws JsonProcessingException {
        return ResponseEntity.ok(chatService.getMessageHistory(receiver, header));
    }

}
