package com.skillverse.service;

import com.skillverse.dto.ChatMessageDTO;
import com.skillverse.model.Chat;
import com.skillverse.model.User;
import com.skillverse.repository.ChatRepository;
import com.skillverse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
        
        return chatRepository.save(chat);
    }
    
    public List<ChatMessageDTO> getChatHistory(Integer userId1, Integer userId2) {
        return chatRepository.findChatBetweenUsers(userId1, userId2).stream()
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
}