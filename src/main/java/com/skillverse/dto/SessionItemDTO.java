package com.skillverse.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SessionItemDTO {
    private Integer sessionId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String meetingLink;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String status; // SCHEDULED, LIVE, COMPLETED, CANCELLED
    private Boolean isJoined;
    private Boolean hasRecording;
}
