package com.skillverse.controller;

import com.skillverse.dto.*;
import com.skillverse.security.UserDetailsServiceImpl;
import com.skillverse.service.LearnerService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/learner")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('Learner')")
public class LearnerController {
    
    @Autowired
    private LearnerService learnerService;
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<LearnerDashboardDTO> getDashboard(Authentication authentication) {
        String email = authentication.getName();
        Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
        LearnerDashboardDTO dashboard = learnerService.getDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }   
    
    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getCourseDetail(@PathVariable Integer courseId,
                                             Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            LearnerCourseDetailDTO detail = learnerService.getCourseDetail(courseId, userId);
            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/sessions/upcoming")
    public ResponseEntity<List<SessionItemDTO>> getMyUpcomingSessions(Authentication authentication) {
        String email = authentication.getName();
        Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
        List<SessionItemDTO> sessions = learnerService.getMyUpcomingSessions(userId);
        return ResponseEntity.ok(sessions);
    }
    
}