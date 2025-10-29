package com.skillverse.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveSessionCreateRequest {
    
    @NotNull(message = "Course ID is required")
    private Integer courseId;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;
    
    @Positive(message = "Duration must be positive")
    private Integer scheduledDuration; // in minutes
    
    private Integer maxParticipants = 100;
    
    private Boolean recordingEnabled = false;
    
    private Boolean autoRecord = false;
}