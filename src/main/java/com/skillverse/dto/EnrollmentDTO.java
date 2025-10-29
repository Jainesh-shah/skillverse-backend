package com.skillverse.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EnrollmentDTO {
    private Integer enrollmentId;
    private Integer courseId;
    private String courseTitle;
    private String courseDescription;
    private String courseThumbnail;
    private String creatorName;
    private Integer creatorId;
    private BigDecimal coursePrice;
    private String paymentStatus;
    private LocalDateTime enrolledAt;
    private Integer totalContent;
    private Integer completedContent;
    private Integer upcomingSessions;
}