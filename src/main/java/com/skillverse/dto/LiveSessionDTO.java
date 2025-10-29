package com.skillverse.dto;

import com.skillverse.model.LiveSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveSessionDTO {
    private Integer sessionId;
    private Integer courseId;
    private String courseTitle;
    private Integer creatorId;
    private String creatorName;
    private String creatorProfileImage;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer scheduledDuration;
    private String roomId;
    private String meetingLink;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private LiveSession.SessionStatus status;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private Boolean recordingEnabled;
    private Boolean autoRecord;
    private Boolean isRecording;
    private List<LiveParticipantDTO> participants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}