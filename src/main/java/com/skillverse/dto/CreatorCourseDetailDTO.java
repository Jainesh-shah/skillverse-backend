package com.skillverse.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreatorCourseDetailDTO {
    private Integer courseId;
    private String title;
    private String description;
    private BigDecimal price;
    private String courseType;
    private String difficultyLevel;
    private String duration;
    private String thumbnailUrl;
    
    // Stats
    private Integer totalEnrollments;
    private Double averageRating;
    private Integer totalReviews;
    private BigDecimal totalRevenue;
    
    // Content
    private List<ContentItemDTO> contents;
    private Integer totalContent;
    
    // Sessions
    private List<SessionItemDTO> upcomingSessions;
    private List<SessionItemDTO> pastSessions;
    
    // Recent activity
    private List<EnrollmentActivityDTO> recentEnrollments;
    private List<ReviewItemDTO> recentReviews;
}