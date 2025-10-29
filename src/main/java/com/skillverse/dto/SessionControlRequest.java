package com.skillverse.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionControlRequest {
    
    @NotNull(message = "Session ID is required")
    private Integer sessionId;
    
    @NotNull(message = "Target user ID is required")
    private Integer targetUserId;
    
    @NotNull(message = "Action is required")
    private ControlAction action;
    
    private String reason; // Optional reason for kick
    
    public enum ControlAction {
        MUTE_AUDIO,
        UNMUTE_AUDIO,
        DISABLE_VIDEO,
        ENABLE_VIDEO,
        KICK_PARTICIPANT,
        GRANT_SPEAK_PERMISSION,
        REVOKE_SPEAK_PERMISSION
    }
}