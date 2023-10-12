package com.masamonoke.simplemessenger.entities.message;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class ChatMessage {
    @Id
    @GeneratedValue
    private Long id;
    private String message;
    private String senderName;
    private String receiverName;
    private MessageType status;
    private LocalDateTime date;
}
