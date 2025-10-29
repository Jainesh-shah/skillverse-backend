package com.skillverse.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EnrollmentActivityDTO {
    private Integer enrollmentId;
    private String learnerName;
    private String learnerEmail;
    private String courseTitle;
    private Integer courseId;
    private LocalDateTime enrolledAt;
    private String paymentStatus;
}