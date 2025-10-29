package com.skillverse.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreatorDashboardDTO {
    private Integer totalCourses;
    private Integer totalStudents;
    private Integer upcomingSessions;
    private Integer totalRevenue;
    private List<CourseStatsDTO> topCourses;
    private List<SessionItemDTO> todaySessions;
    private List<EnrollmentActivityDTO> recentEnrollments;
}
