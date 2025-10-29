package com.skillverse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinSessionResponse {
    private LiveSessionDTO session;
    private LiveParticipantDTO participant;
    private MediaServerConfigDTO mediaConfig;
    private String wsUrl; // WebSocket URL for signaling
    private List<LiveParticipantDTO> existingParticipants;
    private Boolean isCreator;
    private String token; // Session-specific token for WebSocket auth
}