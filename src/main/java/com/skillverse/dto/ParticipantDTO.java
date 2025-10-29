package com.skillverse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDTO {
    private Integer participantId;
    private Integer userId;
    private String userName;
    private String profileImage;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private Integer durationMinutes;
    private Boolean isCurrentlyConnected;
    private String connectionQuality;
    private Boolean canSpeak;
    private Boolean canVideo;
    private Boolean isMuted;
    private Boolean videoDisabled;
}