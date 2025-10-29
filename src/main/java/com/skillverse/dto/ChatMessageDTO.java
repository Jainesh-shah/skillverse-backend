package com.skillverse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Integer messageId;
    private Integer senderId;
    private String senderName;
    private Integer receiverId;
    private String receiverName;
    private String message;
    private LocalDateTime sentAt;
}