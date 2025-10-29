package com.skillverse.dto;

import lombok.Data;
import java.util.List;

@Data
public class LearnerDashboardDTO {
    private Integer totalEnrollments;
    private Integer activeCourses;
    private Integer completedCourses;
    private Integer upcomingSessions;
    private List<EnrollmentDTO> recentEnrollments;
    private List<SessionItemDTO> todaySessions;
    private List<ContentItemDTO> continueWatching;
}