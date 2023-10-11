package com.masamonoke.simplemessenger.entities.message;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {
    private String message;
    private String senderName;
    private String receiverName;
    private MessageType status;
    private String date;
}
