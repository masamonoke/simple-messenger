package com.masamonoke.simplemessenger.api.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.masamonoke.simplemessenger.entities.message.ChatMessage;
import com.masamonoke.simplemessenger.repo.ChatMessageRepo;
import com.masamonoke.simplemessenger.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.List;

import static com.masamonoke.simplemessenger.api.AuthTokenUtils.decodeToken;
import static com.masamonoke.simplemessenger.api.AuthTokenUtils.getTokenFromHeader;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final UserRepo userRepo;
    private final ChatMessageRepo chatMessageRepo;
    private final SimpMessagingTemplate messagingTemplate;

    ChatMessage receivePrivateMessage(ChatMessage message) {
        var receiverName = message.getReceiverName();
        var receiver = userRepo
                .findByUsername(receiverName)
                .orElseThrow(() -> new InvalidParameterException(String.format("Cannot find user with username=%s", receiverName)));
        if (receiver.isPrivateMessageFromFriendsOnly()) {
            var isFriend = false;
            if (!receiver.getFriends().isEmpty()) {
                for (var f : receiver.getFriends()) {
                    if (message.getSenderName().equals(f.getUsername())) {
                        isFriend = true;
                        break;
                    }
                }
            }
            if (!isFriend) {
                throw new IllegalStateException(String.format("User username=%s is not friend of User username=%s", message.getSenderName(), receiverName));
            }
        }
        message.setDate(LocalDateTime.now());
        var savedMessage = chatMessageRepo.save(message);
        messagingTemplate.convertAndSendToUser(message.getReceiverName(), "/private", message); // user/<username>/private
        return savedMessage;
    }

    List<ChatMessage> getMessageHistory(String receiver, String authHeader) throws JsonProcessingException {
        var token = getTokenFromHeader(authHeader);
        var map = decodeToken(token);
        var username = map.get("sub");
        return chatMessageRepo.findAllBySenderNameAndReceiverName(username, receiver);
    }
}
