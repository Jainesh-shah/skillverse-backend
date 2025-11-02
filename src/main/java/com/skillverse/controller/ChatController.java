package com.skillverse.controller;

import com.skillverse.dto.ChatMessageDTO;
import com.skillverse.model.Chat;
import com.skillverse.model.User;
import com.skillverse.security.UserDetailsServiceImpl;
import com.skillverse.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin(origins = "*")
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    // REST API Endpoints - Fixed path to match frontend
    @PostMapping("/chat/send")
    @ResponseBody
    public ResponseEntity<ChatMessageDTO> sendMessage(@RequestParam Integer receiverId,
                                            @RequestParam String message,
                                            Authentication authentication) {
        String email = authentication.getName();
        Integer senderId = userDetailsService.loadUserEntityByEmail(email).getUserId();
        Chat chat = chatService.sendMessage(senderId, receiverId, message);
        
        // Convert to DTO for consistent response
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setMessageId(chat.getMessageId());
        dto.setSenderId(chat.getSender().getUserId());
        dto.setSenderName(chat.getSender().getFirstName() + " " + chat.getSender().getLastName());
        dto.setReceiverId(chat.getReceiver().getUserId());
        dto.setReceiverName(chat.getReceiver().getFirstName() + " " + chat.getReceiver().getLastName());
        dto.setMessage(chat.getMessage());
        dto.setSentAt(chat.getSentAt());
        
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping("/chat/history/{userId}")
    @ResponseBody
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(@PathVariable Integer userId,
                                                                Authentication authentication) {
        String email = authentication.getName();
        Integer currentUserId = userDetailsService.loadUserEntityByEmail(email).getUserId();
        return ResponseEntity.ok(chatService.getChatHistory(currentUserId, userId));
    }
    
    @GetMapping("/chat/users")
    @ResponseBody
    public ResponseEntity<List<User>> getChatUsers(Authentication authentication) {
        String email = authentication.getName();
        Integer currentUserId = userDetailsService.loadUserEntityByEmail(email).getUserId();
        return ResponseEntity.ok(chatService.getChatUsers(currentUserId));
    }
    
    // WebSocket Message Handler
    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload Map<String, Object> messageData) {
        Integer senderId = (Integer) messageData.get("senderId");
        Integer receiverId = (Integer) messageData.get("receiverId");
        String message = (String) messageData.get("message");
        
        chatService.sendMessage(senderId, receiverId, message);
    }
}