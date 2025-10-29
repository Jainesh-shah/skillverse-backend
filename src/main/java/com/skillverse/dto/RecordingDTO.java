package com.skillverse.dto;

import com.skillverse.model.SessionRecording;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingDTO {
    private Integer recordingId;
    private Integer sessionId;
    private String sessionTitle;
    private String courseTitle;
    private String creatorName;
    private String recordingName;
    private String filePath;
    private String fileUrl;
    private BigDecimal fileSizeMb;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer durationMinutes;
    private SessionRecording.RecordingStatus status;
    private String format;
    private String resolution;
    private Boolean isPublic;
    private Boolean requiresEnrollment;
    private Boolean hasAccess; // For learner view
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}