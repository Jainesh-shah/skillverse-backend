package com.skillverse.controller;

import com.skillverse.dto.MessageResponse;
import com.skillverse.dto.RecordingDTO;
import com.skillverse.security.UserDetailsServiceImpl;
import com.skillverse.service.SessionRecordingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recordings")
@CrossOrigin(origins = "*")
public class SessionRecordingController {

    @Autowired
    private SessionRecordingService recordingService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @PostMapping("/session/{sessionId}/start")
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<?> startRecording(@PathVariable Integer sessionId,
                                           Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            RecordingDTO recording = recordingService.startRecording(sessionId, userId);
            return ResponseEntity.ok(recording);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/session/{sessionId}/stop")
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<?> stopRecording(@PathVariable Integer sessionId,
                                          Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            RecordingDTO recording = recordingService.stopRecording(sessionId, userId);
            return ResponseEntity.ok(recording);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<?> getSessionRecordings(@PathVariable Integer sessionId,
                                                  Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            List<RecordingDTO> recordings = recordingService.getRecordingsBySession(sessionId, userId);
            return ResponseEntity.ok(recordings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/{recordingId}")
    public ResponseEntity<?> getRecording(@PathVariable Integer recordingId,
                                         Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            RecordingDTO recording = recordingService.getRecordingById(recordingId, userId);
            return ResponseEntity.ok(recording);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/{recordingId}/track-view")
    public ResponseEntity<?> trackView(@PathVariable Integer recordingId,
                                      @RequestBody Map<String, Integer> payload,
                                      Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            Integer watchDuration = payload.getOrDefault("watchDuration", 0);
            recordingService.trackView(recordingId, userId, watchDuration);
            return ResponseEntity.ok(new MessageResponse("View tracked successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // Webhook endpoint for media server to update recording status
    @PostMapping("/webhook/update")
    public ResponseEntity<?> updateRecordingStatus(@RequestBody Map<String, Object> payload) {
        try {
            Integer recordingId = (Integer) payload.get("recordingId");
            String filePath = (String) payload.get("filePath");
            String fileUrl = (String) payload.get("fileUrl");
            Long fileSizeBytes = ((Number) payload.get("fileSizeBytes")).longValue();

            recordingService.updateRecordingStatus(recordingId, filePath, fileUrl, fileSizeBytes);
            return ResponseEntity.ok(new MessageResponse("Recording status updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}