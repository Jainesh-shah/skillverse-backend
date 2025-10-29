package com.skillverse.dto;

import com.skillverse.model.LiveParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveParticipantDTO {
    private Integer participantId;
    private Integer sessionId;
    private Integer learnerId;
    private String learnerName;
    private String learnerEmail;
    private String learnerProfileImage;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private Integer durationMinutes;
    private Boolean isCurrentlyConnected;
    private LiveParticipant.ConnectionQuality connectionQuality;
    private Boolean canSpeak;
    private Boolean canVideo;
    private Boolean isMuted;
    private Boolean videoDisabled;
    private String peerId;
}