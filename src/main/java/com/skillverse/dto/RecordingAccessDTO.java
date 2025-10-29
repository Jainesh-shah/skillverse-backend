package com.skillverse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingAccessDTO {
    private Integer accessId;
    private Integer recordingId;
    private String recordingName;
    private Integer learnerId;
    private String learnerName;
    private String learnerEmail;
    private LocalDateTime grantedAt;
    private LocalDateTime lastViewedAt;
    private Integer viewCount;
    private Integer watchDurationMinutes;
}