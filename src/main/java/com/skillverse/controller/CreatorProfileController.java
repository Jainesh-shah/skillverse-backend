package com.skillverse.controller;

import com.skillverse.dto.CreatorProfileRequest;
import com.skillverse.dto.CreatorProfileResponse;
import com.skillverse.dto.MessageResponse;
import com.skillverse.security.UserDetailsServiceImpl;
import com.skillverse.service.CreatorProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/creator-profile")
@CrossOrigin(origins = "*")
public class CreatorProfileController {
    
    @Autowired
    private CreatorProfileService creatorProfileService;
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @GetMapping("/me")
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            CreatorProfileResponse profile = creatorProfileService.getCreatorProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/complete")
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<?> completeProfile(@RequestBody CreatorProfileRequest request,
                                             Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            CreatorProfileResponse profile = creatorProfileService.createOrUpdateProfile(userId, request);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @PutMapping("/update")
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<?> updateProfile(@RequestBody CreatorProfileRequest request,
                                          Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            CreatorProfileResponse profile = creatorProfileService.createOrUpdateProfile(userId, request);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/check")
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<Boolean> checkProfile(Authentication authentication) {
        String email = authentication.getName();
        Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
        boolean hasProfile = creatorProfileService.hasCreatorProfile(userId);
        return ResponseEntity.ok(hasProfile);
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfileByUserId(@PathVariable Integer userId) {
        try {
            CreatorProfileResponse profile = creatorProfileService.getCreatorProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}