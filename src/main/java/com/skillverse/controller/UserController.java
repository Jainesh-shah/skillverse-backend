package com.skillverse.controller;

import com.skillverse.dto.MessageResponse;
import com.skillverse.model.User;
import com.skillverse.security.UserDetailsServiceImpl;
import com.skillverse.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        User user = userDetailsService.loadUserEntityByEmail(email);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
    
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody User updatedUser,
                                          Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            User user = userService.updateProfile(userId, updatedUser);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}