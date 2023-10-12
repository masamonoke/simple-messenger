package com.masamonoke.simplemessenger.repo;

import com.masamonoke.simplemessenger.entities.message.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepo extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllBySenderNameAndReceiverName(String sender, String receiver);
    List<ChatMessage> findAllBySenderName(String sender);
}
