package com.skillverse.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class LearnerCourseDetailDTO {
    private Integer courseId;
    private String title;
    private String description;
    private BigDecimal price;
    private String courseType;
    private String difficultyLevel;
    private String duration;
    private String thumbnailUrl;
    
    // Creator info
    private Integer creatorId;
    private String creatorName;
    private String creatorEmail;
    private String creatorBio;
    
    // Course content
    private List<ContentItemDTO> contents;
    
    //Live sessions
    private List<SessionItemDTO> upcomingSessions;
    private List<SessionItemDTO> pastSessions;
    
    // Progress
    private Integer totalLessons;
    private Integer completedLessons;
    private Integer progressPercentage;
}
