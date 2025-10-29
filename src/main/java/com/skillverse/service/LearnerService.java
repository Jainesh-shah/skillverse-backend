package com.skillverse.service;

import com.skillverse.dto.*;
import com.skillverse.model.*;
import com.skillverse.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LearnerService {
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private CourseContentRepository courseContentRepository;
    
    @Autowired
    private LiveSessionRepository liveSessionRepository;

    
    @Autowired
    private LiveParticipantRepository liveParticipantRepository;
    
    
    public LearnerDashboardDTO getDashboard(Integer learnerId) {
        LearnerDashboardDTO dashboard = new LearnerDashboardDTO();
        
        List<Enrollment> enrollments = enrollmentRepository.findByLearner_UserId(learnerId);
        dashboard.setTotalEnrollments(enrollments.size());
        dashboard.setActiveCourses(enrollments.size());
        dashboard.setCompletedCourses(0); // TODO: Implement completion tracking
        
        // Get upcoming sessions
        List<LiveSession> upcomingSessions = liveSessionRepository
            .findByStartTimeAfter(LocalDateTime.now());
        
        List<SessionItemDTO> todaySessions = upcomingSessions.stream()
            .filter(session -> enrollments.stream()
                .anyMatch(e -> e.getCourse().getCourseId().equals(session.getCourse().getCourseId())))
            .filter(session -> session.getStartTime().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
            .map(this::mapToSessionItemDTO)
            .collect(Collectors.toList());
        
        dashboard.setUpcomingSessions(todaySessions.size());
        dashboard.setTodaySessions(todaySessions);
        
        
        List<EnrollmentDTO> recentEnrollments = enrollments.stream()
            .sorted((e1, e2) -> e2.getEnrolledAt().compareTo(e1.getEnrolledAt()))
            .limit(5)
            .map(this::mapToEnrollmentDTO)
            .collect(Collectors.toList());
        dashboard.setRecentEnrollments(recentEnrollments);
        
        return dashboard;
    }
    
    public LearnerCourseDetailDTO getCourseDetail(Integer courseId, Integer learnerId) {
        // Check if enrolled
        if (!enrollmentRepository.existsByCourse_CourseIdAndLearner_UserId(courseId, learnerId)) {
            throw new RuntimeException("Not enrolled in this course");
        }
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
        
        LearnerCourseDetailDTO dto = new LearnerCourseDetailDTO();
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
        
        // Creator info
        User creator = course.getCreator().getUser();
        dto.setCreatorId(course.getCreator().getCreatorId());
        dto.setCreatorName(creator.getFirstName() + " " + creator.getLastName());
        dto.setCreatorEmail(creator.getEmail());
        dto.setCreatorBio(creator.getBio());
        
        // Course contents
        List<CourseContent> contents = courseContentRepository.findByCourse_CourseId(courseId);
        List<ContentItemDTO> contentDTOs = contents.stream()
            .map(this::mapToContentItemDTO)
            .collect(Collectors.toList());
        dto.setContents(contentDTOs);
        dto.setTotalLessons(contents.size());
        dto.setCompletedLessons(0); // TODO: Track completion
        dto.setProgressPercentage(0);

         // Live sessions
        List<LiveSession> allSessions = liveSessionRepository.findByCourse_CourseId(courseId);
        LocalDateTime now = LocalDateTime.now();
        
        List<SessionItemDTO> upcomingSessions = allSessions.stream()
            .filter(s -> s.getStartTime().isAfter(now))
            .map(s -> mapToSessionItemDTO(s, learnerId))
            .collect(Collectors.toList());
        dto.setUpcomingSessions(upcomingSessions);
        
        List<SessionItemDTO> pastSessions = allSessions.stream()
            .filter(s -> s.getStartTime().isBefore(now))
            .map(s -> mapToSessionItemDTO(s, learnerId))
            .collect(Collectors.toList());
        dto.setPastSessions(pastSessions);
       
        
        return dto;
    }

    public List<SessionItemDTO> getMyUpcomingSessions(Integer learnerId) {
        List<Enrollment> enrollments = enrollmentRepository.findByLearner_UserId(learnerId);
        List<Integer> courseIds = enrollments.stream()
            .map(e -> e.getCourse().getCourseId())
            .collect(Collectors.toList());
        
        List<LiveSession> sessions = liveSessionRepository.findByStartTimeAfter(LocalDateTime.now());
        
        return sessions.stream()
            .filter(s -> courseIds.contains(s.getCourse().getCourseId()))
            .map(s -> mapToSessionItemDTO(s, learnerId))
            .collect(Collectors.toList());
    }
    
    private EnrollmentDTO mapToEnrollmentDTO(Enrollment enrollment) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setEnrollmentId(enrollment.getEnrollId());
        dto.setCourseId(enrollment.getCourse().getCourseId());
        dto.setCourseTitle(enrollment.getCourse().getTitle());
        dto.setCourseDescription(enrollment.getCourse().getDescription());
        dto.setCourseThumbnail(enrollment.getCourse().getThumbnailUrl());
        
        User creator = enrollment.getCourse().getCreator().getUser();
        dto.setCreatorName(creator.getFirstName() + " " + creator.getLastName());
        dto.setCreatorId(enrollment.getCourse().getCreator().getCreatorId());
        
        dto.setCoursePrice(enrollment.getCourse().getPrice());
        dto.setPaymentStatus(enrollment.getPaymentStatus().name());
        dto.setEnrolledAt(enrollment.getEnrolledAt());
        
        // Count content
        int totalContent = courseContentRepository.findByCourse_CourseId(enrollment.getCourse().getCourseId()).size();
        dto.setTotalContent(totalContent);
        dto.setCompletedContent(0); // TODO: Track completion
        
        // Count upcoming sessions
        int upcomingSessions = liveSessionRepository.findByStartTimeAfter(LocalDateTime.now()).stream()
            .filter(s -> s.getCourse().getCourseId().equals(enrollment.getCourse().getCourseId()))
            .collect(Collectors.toList()).size();
        dto.setUpcomingSessions(upcomingSessions);
        
        return dto;
    }
    
    private ContentItemDTO mapToContentItemDTO(CourseContent content) {
        ContentItemDTO dto = new ContentItemDTO();
        dto.setContentId(content.getContentId());
        dto.setContentTitle(content.getContentTitle());
        dto.setVideoUrl(content.getVideoUrl());
        dto.setDuration(content.getDuration());
        dto.setUploadedAt(content.getUploadedAt());
        dto.setIsCompleted(false); // TODO: Track completion
        return dto;
    }

    private SessionItemDTO mapToSessionItemDTO(LiveSession session) {
        return mapToSessionItemDTO(session, null);
    }
    
    private SessionItemDTO mapToSessionItemDTO(LiveSession session, Integer learnerId) {
        SessionItemDTO dto = new SessionItemDTO();
        dto.setSessionId(session.getSessionId());
        dto.setTitle(session.getTitle());
        dto.setDescription(session.getDescription());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setMeetingLink(session.getMeetingLink());
        dto.setMaxParticipants(session.getMaxParticipants());
        
        // Count participants
        int participants = liveParticipantRepository.findBySession_SessionId(session.getSessionId()).size();
        dto.setCurrentParticipants(participants);
        
        // Determine status
        LocalDateTime now = LocalDateTime.now();
        if (session.getStartTime().isAfter(now)) {
            dto.setStatus("SCHEDULED");
        } else if (session.getEndTime() != null && session.getEndTime().isBefore(now)) {
            dto.setStatus("COMPLETED");
        } else {
            dto.setStatus("LIVE");
        }
        
        // Check if learner joined
        if (learnerId != null) {
            boolean isJoined = liveParticipantRepository
                .existsBySession_SessionIdAndLearner_UserId(session.getSessionId(), learnerId);
            dto.setIsJoined(isJoined);
        }
        
        dto.setHasRecording(false); // TODO: Check recordings
        
        return dto;
    }
    
}