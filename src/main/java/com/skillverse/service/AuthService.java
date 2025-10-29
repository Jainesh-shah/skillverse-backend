package com.skillverse.service;

import com.skillverse.dto.JwtResponse;
import com.skillverse.dto.LoginRequest;
import com.skillverse.dto.RegisterRequest;
import com.skillverse.model.CreatorProfile;
import com.skillverse.model.User;
import com.skillverse.repository.CreatorProfileRepository;
import com.skillverse.repository.UserRepository;
import com.skillverse.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CreatorProfileRepository creatorProfileRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Transactional
    public JwtResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setUserType(User.UserType.valueOf(request.getUserType()));
        user.setBio(request.getBio());
        
        user = userRepository.save(user);
        
        // Create creator profile if user type is Creator
        if (user.getUserType() == User.UserType.Creator) {
            CreatorProfile creatorProfile = new CreatorProfile();
            creatorProfile.setUser(user);
            creatorProfile.setVerified(false);
            creatorProfileRepository.save(creatorProfile);
        }
        
        String token = jwtUtil.generateToken(user.getEmail(), user.getUserType().name(), user.getUserId());
        
        return new JwtResponse(token, user.getUserId(), user.getEmail(), user.getUserType().name());
    }
    
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String token = jwtUtil.generateToken(user.getEmail(), user.getUserType().name(), user.getUserId());
        
        return new JwtResponse(token, user.getUserId(), user.getEmail(), user.getUserType().name());
    }
}