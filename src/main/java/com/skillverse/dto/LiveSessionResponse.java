package com.skillverse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveSessionResponse {
    private Integer sessionId;
    private Integer courseId;
    private String courseTitle;
    private Integer creatorId;
    private String creatorName;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer scheduledDuration;
    private String roomId;
    private String meetingLink;
    private Integer maxParticipants;
    private String status;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private Boolean recordingEnabled;
    private Boolean autoRecord;
    private Integer currentParticipants;
    private LocalDateTime createdAt;
}
