package com.skillverse.controller;

import com.skillverse.dto.*;
import com.skillverse.security.UserDetailsServiceImpl;
import com.skillverse.service.LiveParticipantService;
import com.skillverse.service.LiveSessionService;
import com.skillverse.service.SessionControlService;
import com.skillverse.service.UserService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/live-sessions")
@CrossOrigin(origins = "*")
public class LiveSessionController {

    @Autowired
    private LiveSessionService liveSessionService;

    @Autowired
    private LiveParticipantService liveParticipantService;

    @Autowired
    private SessionControlService sessionControlService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserService userService;


    @PostMapping
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<?> createSession(@Valid @RequestBody LiveSessionCreateRequest request,
                                          Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            LiveSessionDTO session = liveSessionService.createSession(request, userId);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/start")
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<?> startSession(@PathVariable Integer sessionId,
                                         Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            LiveSessionDTO session = liveSessionService.startSession(sessionId, userId);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/end")
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<?> endSession(@PathVariable Integer sessionId,
                                       Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            LiveSessionDTO session = liveSessionService.endSession(sessionId, userId);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getSession(@PathVariable Integer sessionId) {
        try {
            LiveSessionDTO session = liveSessionService.getSessionById(sessionId);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LiveSessionDTO>> getSessionsByCourse(@PathVariable Integer courseId) {
        List<LiveSessionDTO> sessions = liveSessionService.getSessionsByCourse(courseId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<List<LiveSessionDTO>> getUpcomingSessions(Authentication authentication) {
        String email = authentication.getName();
        Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
        List<LiveSessionDTO> sessions = liveSessionService.getUpcomingSessions(userId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/live")
    public ResponseEntity<List<LiveSessionDTO>> getLiveSessions() {
        List<LiveSessionDTO> sessions = liveSessionService.getLiveSessions();
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/{sessionId}/join")
    public ResponseEntity<?> joinSession(@PathVariable Integer sessionId,
                                        Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            JoinSessionResponse response = liveParticipantService.joinSession(sessionId, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
             e.printStackTrace(); 
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/leave")
    public ResponseEntity<?> leaveSession(@PathVariable Integer sessionId,
                                         Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            liveParticipantService.leaveSession(sessionId, userId);
            return ResponseEntity.ok(new MessageResponse("Left session successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/{sessionId}/participants")
    public ResponseEntity<List<LiveParticipantDTO>> getParticipants(@PathVariable Integer sessionId) {
        List<LiveParticipantDTO> participants = liveParticipantService.getSessionParticipants(sessionId);
        return ResponseEntity.ok(participants);
    }

    @GetMapping("/creator/my-sessions")
    public ResponseEntity<List<LiveSessionResponse>> getCreatorSessions(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Integer userId = userService.getUserByEmail(userDetails.getUsername()).getUserId();
        // Get creator ID from user ID (you'll need to implement this)
        List<LiveSessionResponse> sessions = liveSessionService.getCreatorSessions(userId);
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/control")
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<?> controlSession(@Valid @RequestBody SessionControlRequest request,
                                           Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            sessionControlService.executeControl(request, userId);
            return ResponseEntity.ok(new MessageResponse("Control executed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}