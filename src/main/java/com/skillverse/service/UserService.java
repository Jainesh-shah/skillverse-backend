package com.skillverse.service;

import com.skillverse.model.User;
import com.skillverse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @Transactional
    public User updateProfile(Integer userId, User updatedUser) {
        User user = getUserById(userId);
        
        if (updatedUser.getFirstName() != null) user.setFirstName(updatedUser.getFirstName());
        if (updatedUser.getLastName() != null) user.setLastName(updatedUser.getLastName());
        if (updatedUser.getPhone() != null) user.setPhone(updatedUser.getPhone());
        if (updatedUser.getBio() != null) user.setBio(updatedUser.getBio());
        if (updatedUser.getProfileImage() != null) user.setProfileImage(updatedUser.getProfileImage());
        
        return userRepository.save(user);
    }
}