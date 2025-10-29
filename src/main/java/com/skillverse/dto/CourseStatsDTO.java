package com.skillverse.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CourseStatsDTO {
    private Integer courseId;
    private String title;
    private String thumbnailUrl;
    private Integer totalEnrollments;
    private Double averageRating;
    private BigDecimal revenue;
    private Integer totalContent;
    private Integer upcomingSessions;
    private String courseType;
    private String difficultyLevel;
}