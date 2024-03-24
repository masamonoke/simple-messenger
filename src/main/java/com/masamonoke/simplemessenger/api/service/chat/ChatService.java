package com.masamonoke.simplemessenger.api.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.masamonoke.simplemessenger.entities.message.ChatMessage;

import java.util.List;

public interface ChatService {
    ChatMessage receivePrivateMessage(ChatMessage message);

    List<ChatMessage> getMessageHistory(String receiver, String authHeader) throws JsonProcessingException;
}
