package com.skillverse.service;

import com.skillverse.dto.*;
import com.skillverse.exception.CreatorProfileNotFoundException;
import com.skillverse.model.*;
import com.skillverse.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreatorService {

    @Autowired
    private CreatorProfileRepository creatorProfileRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LiveSessionRepository liveSessionRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CourseContentRepository courseContentRepository;

    public CreatorDashboardDTO getDashboard(Integer userId) {
        CreatorProfile creator = creatorProfileRepository.findByUser_UserId(userId)
                .orElse(null);

        if (creator == null) {
            throw new CreatorProfileNotFoundException("Creator profile not found");
        }

        CreatorDashboardDTO dashboard = new CreatorDashboardDTO();

        // Get all creator's courses
        List<Course> courses = courseRepository.findByCreator_CreatorId(creator.getCreatorId());
        dashboard.setTotalCourses(courses.size());

        // Calculate total students (unique enrollments across all courses)
        int totalStudents = courses.stream()
                .mapToInt(course -> enrollmentRepository.findByCourse_CourseId(course.getCourseId()).size())
                .sum();
        dashboard.setTotalStudents(totalStudents);

        // Get upcoming sessions
        List<LiveSession> upcomingSessions = liveSessionRepository
                .findByCreator_CreatorId(creator.getCreatorId()).stream()
                .filter(s -> s.getStartTime().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
        dashboard.setUpcomingSessions(upcomingSessions.size());

                // Today's sessions
        List<SessionItemDTO> todaySessions = upcomingSessions.stream()
                .filter(session -> session.getStartTime().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .map(this::mapToSessionItemDTO)
                .collect(Collectors.toList());
        dashboard.setTodaySessions(todaySessions);

        

        // Calculate total revenue (mock - in production, aggregate from payments)
        BigDecimal totalRevenue = courses.stream()
                .map(course -> {
                    int enrollments = enrollmentRepository.findByCourse_CourseId(course.getCourseId()).size();
                    return course.getPrice().multiply(new BigDecimal(enrollments));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dashboard.setTotalRevenue(totalRevenue.intValue());

        // Top courses by enrollment
        List<CourseStatsDTO> topCourses = courses.stream()
                .map(this::mapToCourseStatsDTO)
                .sorted((c1, c2) -> c2.getTotalEnrollments().compareTo(c1.getTotalEnrollments()))
                .limit(5)
                .collect(Collectors.toList());
        dashboard.setTopCourses(topCourses);

        List<EnrollmentActivityDTO> recentEnrollments = courses.stream()
                .flatMap(course -> enrollmentRepository.findByCourse_CourseId(course.getCourseId()).stream())
                .sorted((e1, e2) -> e2.getEnrolledAt().compareTo(e1.getEnrolledAt()))
                .limit(10)
                .map(this::mapToEnrollmentActivityDTO)
                .collect(Collectors.toList());
        dashboard.setRecentEnrollments(recentEnrollments);

        return dashboard;
    }

    public CreatorCourseDetailDTO getCreatorCourseDetail(Integer courseId, Integer userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Verify ownership
        if (!course.getCreator().getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to view this course");
        }

        CreatorCourseDetailDTO dto = new CreatorCourseDetailDTO();
        dto.setCourseId(course.getCourseId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setPrice(course.getPrice());
        dto.setCourseType(course.getCourseType().name());
        if (course.getDifficultyLevel() != null) {
            dto.setDifficultyLevel(course.getDifficultyLevel().name());
        }
        dto.setDuration(course.getDuration());
        dto.setThumbnailUrl(course.getThumbnailUrl());

        // Stats
        List<Enrollment> enrollments = enrollmentRepository.findByCourse_CourseId(courseId);
        dto.setTotalEnrollments(enrollments.size());

        Double avgRating = reviewRepository.getAverageRatingByCourse(courseId);
        dto.setAverageRating(avgRating != null ? avgRating : 0.0);

        List<Review> reviews = reviewRepository.findByCourse_CourseId(courseId);
        dto.setTotalReviews(reviews.size());

        BigDecimal revenue = course.getPrice().multiply(new BigDecimal(enrollments.size()));
        dto.setTotalRevenue(revenue);

        // Content
        List<CourseContent> contents = courseContentRepository.findByCourse_CourseId(courseId);
        List<ContentItemDTO> contentDTOs = contents.stream()
                .map(this::mapToContentItemDTO)
                .collect(Collectors.toList());
        dto.setContents(contentDTOs);
        dto.setTotalContent(contents.size());

        // Sessions
        List<LiveSession> allSessions = liveSessionRepository.findByCourse_CourseId(courseId);
        LocalDateTime now = LocalDateTime.now();

        List<SessionItemDTO> upcomingSessions = allSessions.stream()
                .filter(s -> s.getStartTime().isAfter(now))
                .map(this::mapToSessionItemDTO)
                .collect(Collectors.toList());
        dto.setUpcomingSessions(upcomingSessions);

        List<SessionItemDTO> pastSessions = allSessions.stream()
                .filter(s -> s.getStartTime().isBefore(now))
                .map(this::mapToSessionItemDTO)
                .collect(Collectors.toList());
        dto.setPastSessions(pastSessions);



        // Recent enrollments
        List<EnrollmentActivityDTO> recentEnrollments = enrollments.stream()
                .sorted((e1, e2) -> e2.getEnrolledAt().compareTo(e1.getEnrolledAt()))
                .limit(10)
                .map(this::mapToEnrollmentActivityDTO)
                .collect(Collectors.toList());
        dto.setRecentEnrollments(recentEnrollments);

        // Recent reviews
        List<ReviewItemDTO> recentReviews = reviews.stream()
                .sorted((r1, r2) -> r2.getReviewedAt().compareTo(r1.getReviewedAt()))
                .limit(10)
                .map(this::mapToReviewItemDTO)
                .collect(Collectors.toList());
        dto.setRecentReviews(recentReviews);

        return dto;
    }

    private CourseStatsDTO mapToCourseStatsDTO(Course course) {
        CourseStatsDTO dto = new CourseStatsDTO();
        dto.setCourseId(course.getCourseId());
        dto.setTitle(course.getTitle());
        dto.setThumbnailUrl(course.getThumbnailUrl());
        dto.setCourseType(course.getCourseType().name());
        if (course.getDifficultyLevel() != null) {
            dto.setDifficultyLevel(course.getDifficultyLevel().name());
        }

        int enrollments = enrollmentRepository.findByCourse_CourseId(course.getCourseId()).size();
        dto.setTotalEnrollments(enrollments);

        Double avgRating = reviewRepository.getAverageRatingByCourse(course.getCourseId());
        dto.setAverageRating(avgRating != null ? avgRating : 0.0);

        BigDecimal revenue = course.getPrice().multiply(new BigDecimal(enrollments));
        dto.setRevenue(revenue);

        int contentCount = courseContentRepository.findByCourse_CourseId(course.getCourseId()).size();
        dto.setTotalContent(contentCount);

        int upcomingSessions = (int) liveSessionRepository.findByCourse_CourseId(course.getCourseId()).stream()
                .filter(s -> s.getStartTime().isAfter(LocalDateTime.now()))
                .count();
        dto.setUpcomingSessions(upcomingSessions);

        return dto;
    }



    private EnrollmentActivityDTO mapToEnrollmentActivityDTO(Enrollment enrollment) {
        EnrollmentActivityDTO dto = new EnrollmentActivityDTO();
        dto.setEnrollmentId(enrollment.getEnrollId());
        dto.setLearnerName(enrollment.getLearner().getFirstName() + " " + enrollment.getLearner().getLastName());
        dto.setLearnerEmail(enrollment.getLearner().getEmail());
        dto.setCourseTitle(enrollment.getCourse().getTitle());
        dto.setCourseId(enrollment.getCourse().getCourseId());
        dto.setEnrolledAt(enrollment.getEnrolledAt());
        dto.setPaymentStatus(enrollment.getPaymentStatus().name());
        return dto;
    }

    private ContentItemDTO mapToContentItemDTO(CourseContent content) {
        ContentItemDTO dto = new ContentItemDTO();
        dto.setContentId(content.getContentId());
        dto.setContentTitle(content.getContentTitle());
        dto.setVideoUrl(content.getVideoUrl());
        dto.setDuration(content.getDuration());
        dto.setUploadedAt(content.getUploadedAt());
        return dto;
    }

    private ReviewItemDTO mapToReviewItemDTO(Review review) {
        ReviewItemDTO dto = new ReviewItemDTO();
        dto.setReviewId(review.getReviewId());
        dto.setLearnerName(review.getLearner().getFirstName() + " " + review.getLearner().getLastName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setReviewedAt(review.getReviewedAt());
        return dto;
    }

    private SessionItemDTO mapToSessionItemDTO(LiveSession session) {
        SessionItemDTO dto = new SessionItemDTO();
        dto.setSessionId(session.getSessionId());
        dto.setTitle(session.getTitle());
        dto.setDescription(session.getDescription());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setMeetingLink(session.getMeetingLink());
        dto.setMaxParticipants(session.getMaxParticipants());

        LocalDateTime now = LocalDateTime.now();
        if (session.getStartTime().isAfter(now)) {
            dto.setStatus("SCHEDULED");
        } else if (session.getEndTime() != null && session.getEndTime().isBefore(now)) {
            dto.setStatus("COMPLETED");
        } else {
            dto.setStatus("LIVE");
        }

        return dto;
    }
}