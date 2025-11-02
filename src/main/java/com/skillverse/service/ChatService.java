package com.skillverse.service;

import com.skillverse.dto.ChatMessageDTO;
import com.skillverse.model.Chat;
import com.skillverse.model.User;
import com.skillverse.repository.ChatRepository;
import com.skillverse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {
    
    @Autowired
    private ChatRepository chatRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Transactional
    public Chat sendMessage(Integer senderId, Integer receiverId, String message) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        
        Chat chat = new Chat();
        chat.setSender(sender);
        chat.setReceiver(receiver);
        chat.setMessage(message);
        
        Chat savedChat = chatRepository.save(chat);
        
        // Send real-time notification via WebSocket to receiver
        ChatMessageDTO dto = convertToDTO(savedChat);
        
        System.out.println("=== SENDING WEBSOCKET MESSAGE ===");
        System.out.println("From: " + senderId + " (" + sender.getFirstName() + ")");
        System.out.println("To: " + receiverId + " (" + receiver.getFirstName() + ")");
        System.out.println("Message: " + message);
        System.out.println("DTO: " + dto);
        System.out.println("Destination: /user/" + receiverId + "/queue/messages");
        
        try {
            messagingTemplate.convertAndSendToUser(
                receiverId.toString(), 
                "/queue/messages", 
                dto
            );
            System.out.println("✅ Message sent to receiver: " + receiverId);
        } catch (Exception e) {
            System.err.println("❌ Failed to send to receiver: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Also send to sender for confirmation
        try {
            messagingTemplate.convertAndSendToUser(
                senderId.toString(), 
                "/queue/messages", 
                dto
            );
            System.out.println("✅ Message sent to sender: " + senderId);
        } catch (Exception e) {
            System.err.println("❌ Failed to send to sender: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=================================");
        
        return savedChat;
    }
    
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getChatHistory(Integer userId1, Integer userId2) {
        List<Chat> chats = chatRepository.findChatBetweenUsers(userId1, userId2);
        return chats.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private ChatMessageDTO convertToDTO(Chat chat) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setMessageId(chat.getMessageId());
        dto.setSenderId(chat.getSender().getUserId());
        dto.setSenderName(chat.getSender().getFirstName() + " " + chat.getSender().getLastName());
        dto.setReceiverId(chat.getReceiver().getUserId());
        dto.setReceiverName(chat.getReceiver().getFirstName() + " " + chat.getReceiver().getLastName());
        dto.setMessage(chat.getMessage());
        dto.setSentAt(chat.getSentAt());
        return dto;
    }
    
    @Transactional(readOnly = true)
    public List<User> getChatUsers(Integer userId) {
        try {
            // Get users who have exchanged messages with the current user
            List<User> users = chatRepository.findUsersWithChatHistory(userId);
            return users != null ? users : new java.util.ArrayList<>();
        } catch (Exception e) {
            // Return empty list if no chat history exists
            return new java.util.ArrayList<>();
        }
    }
}