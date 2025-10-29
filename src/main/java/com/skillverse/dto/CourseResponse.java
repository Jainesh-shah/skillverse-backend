package com.skillverse.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseResponse {
    private Integer courseId;
    private Integer creatorId;
    private String creatorName;
    private Integer skillId;
    private String skillName;
    private String title;
    private String description;
    private BigDecimal price;
    private String courseType;
    private String difficultyLevel;
    private String duration;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private Double averageRating;
    private Integer totalEnrollments;
}