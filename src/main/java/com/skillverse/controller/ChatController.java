package com.skillverse.controller;

import com.skillverse.dto.ChatMessageDTO;
import com.skillverse.model.Chat;
import com.skillverse.security.UserDetailsServiceImpl;
import com.skillverse.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*")
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @PostMapping("/send")
    public ResponseEntity<Chat> sendMessage(@RequestParam Integer receiverId,
                                            @RequestParam String message,
                                            Authentication authentication) {
        String email = authentication.getName();
        Integer senderId = userDetailsService.loadUserEntityByEmail(email).getUserId();
        Chat chat = chatService.sendMessage(senderId, receiverId, message);
        return ResponseEntity.ok(chat);
    }
    
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(@PathVariable Integer userId,
                                                                Authentication authentication) {
        String email = authentication.getName();
        Integer currentUserId = userDetailsService.loadUserEntityByEmail(email).getUserId();
        return ResponseEntity.ok(chatService.getChatHistory(currentUserId, userId));
    }
}