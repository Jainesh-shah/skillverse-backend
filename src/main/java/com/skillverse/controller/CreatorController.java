package com.skillverse.controller;

import com.skillverse.dto.*;
import com.skillverse.security.UserDetailsServiceImpl;
import com.skillverse.service.CreatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('Creator')")
@RequestMapping("/creator")
public class CreatorController {
    
    @Autowired
    private CreatorService creatorService;
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<CreatorDashboardDTO> getDashboard(Authentication authentication) {
        String email = authentication.getName();
        Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
        CreatorDashboardDTO dashboard = creatorService.getDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }
    
    @GetMapping("/course/{courseId}/details")
    public ResponseEntity<?> getCourseDetail(@PathVariable Integer courseId,
                                             Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            CreatorCourseDetailDTO detail = creatorService.getCreatorCourseDetail(courseId, userId);
            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}