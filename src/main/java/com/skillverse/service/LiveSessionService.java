package com.skillverse.service;

import com.skillverse.dto.*;
import com.skillverse.exception.ResourceNotFoundException;
import com.skillverse.model.*;
import com.skillverse.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.time.Duration;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LiveSessionService {

    @Autowired
    private LiveSessionRepository liveSessionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CreatorProfileRepository creatorProfileRepository;

    @Autowired
    private LiveParticipantRepository liveParticipantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private EnrollmentRepository enrollmentRepository;
    

    @Transactional
    public LiveSessionDTO createSession(LiveSessionCreateRequest request, Integer creatorUserId) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        CreatorProfile creator = creatorProfileRepository.findByUser_UserId(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Creator profile not found"));

        // Verify creator owns the course
        if (!course.getCreator().getCreatorId().equals(creator.getCreatorId())) {
            throw new IllegalArgumentException("You can only create sessions for your own courses");
        }

        LiveSession session = new LiveSession();
        session.setCourse(course);
        session.setCreator(creator);
        session.setTitle(request.getTitle());
        session.setDescription(request.getDescription());
        session.setStartTime(request.getStartTime());
        session.setScheduledDuration(request.getScheduledDuration());
        session.setMaxParticipants(request.getMaxParticipants());
        session.setRecordingEnabled(request.getRecordingEnabled());
        session.setAutoRecord(request.getAutoRecord());
        session.setRoomId(UUID.randomUUID().toString());
        session.setStatus(LiveSession.SessionStatus.SCHEDULED);

        if (request.getScheduledDuration() != null) {
            session.setEndTime(request.getStartTime().plusMinutes(request.getScheduledDuration()));
        }

        LiveSession saved = liveSessionRepository.save(session);
        log.info("Created live session {} for course {}", saved.getSessionId(), course.getCourseId());

        return convertToDTO(saved);
    }

    
@Transactional
public LiveSessionDTO startSession(Integer sessionId, Integer creatorUserId) {
    LiveSession session = liveSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

    if (!session.getCreator().getUser().getUserId().equals(creatorUserId)) {
        throw new IllegalArgumentException("Only the creator can start the session");
    }

    if (session.getStatus() != LiveSession.SessionStatus.SCHEDULED) {
        throw new IllegalStateException("Session already started or completed");
    }

    session.setStatus(LiveSession.SessionStatus.LIVE);
    session.setActualStartTime(LocalDateTime.now());

    // ADDED: Update all waiting participants to connected
    List<LiveParticipant> waitingParticipants = liveParticipantRepository
            .findBySession_SessionId(sessionId);
    
    for (LiveParticipant p : waitingParticipants) {
        if (p.getJoinedAt() == null) {
            p.setJoinedAt(LocalDateTime.now());
            p.setIsCurrentlyConnected(true);
        }
    }
    liveParticipantRepository.saveAll(waitingParticipants);

    LiveSession updated = liveSessionRepository.save(session);
    log.info("Started live session {}", sessionId);

    return convertToDTO(updated);
}
    @Transactional
    public LiveSessionDTO endSession(Integer sessionId, Integer creatorUserId) {
        LiveSession session = liveSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getCreator().getUser().getUserId().equals(creatorUserId)) {
            throw new IllegalArgumentException("Only the creator can end the session");
        }

        if (session.getStatus() != LiveSession.SessionStatus.LIVE) {
            throw new IllegalStateException("Session is not live");
        }

        session.setStatus(LiveSession.SessionStatus.COMPLETED);
        session.setActualEndTime(LocalDateTime.now());

        // Disconnect all participants
        List<LiveParticipant> participants = liveParticipantRepository
                .findBySession_SessionIdAndIsCurrentlyConnected(sessionId, true);
        
        for (LiveParticipant p : participants) {
            p.setIsCurrentlyConnected(false);
            p.setLeftAt(LocalDateTime.now());
            if (p.getJoinedAt() != null) {
                long duration = java.time.Duration.between(p.getJoinedAt(), p.getLeftAt()).toMinutes();
                p.setDurationMinutes((int) duration);
            }
        }
        liveParticipantRepository.saveAll(participants);

        LiveSession updated = liveSessionRepository.save(session);
        log.info("Ended live session {}", sessionId);

        return convertToDTO(updated);
    }

    @Transactional
    public ParticipantDTO joinSession(JoinSessionRequest request, Integer userId) {
        LiveSession session = liveSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() == LiveSession.SessionStatus.COMPLETED ||
                session.getStatus() == LiveSession.SessionStatus.CANCELLED) {
            throw new RuntimeException("Session is not available");
        }

        Integer creatorUserId = session.getCreator().getUser() != null
                ? session.getCreator().getUser().getUserId()
                : null;

        boolean isCreatorJoining = creatorUserId != null && creatorUserId.equals(userId);

        if (!isCreatorJoining) {
            boolean enrolled = enrollmentRepository.existsByCourse_CourseIdAndLearner_UserId(
                    session.getCourse().getCourseId(), userId);
            if (!enrolled) {
                throw new RuntimeException("You must be enrolled in the course to join");
            }
        }

        Integer currentCount = liveParticipantRepository.countActiveParticipants(request.getSessionId());
        if (currentCount >= session.getMaxParticipants()) {
            throw new RuntimeException("Session is full");
        }

        LiveParticipant participant = liveParticipantRepository
                .findBySession_SessionIdAndLearner_UserId(request.getSessionId(), userId)
                .orElseGet(() -> {
                    LiveParticipant newParticipant = new LiveParticipant();
                    newParticipant.setSession(session);
                    User user = new User();
                    user.setUserId(userId);
                    newParticipant.setLearner(user);
                    return newParticipant;
                });

        participant.setJoinedAt(LocalDateTime.now());
        participant.setIsCurrentlyConnected(true);
        participant.setCanSpeak(true);
        participant.setCanVideo(true);
        participant.setIsMuted(false);
        participant.setVideoDisabled(!request.getVideoEnabled());

        LiveParticipant saved = liveParticipantRepository.saveAndFlush(participant);

        messagingTemplate.convertAndSend(
                "/topic/session/" + request.getSessionId() + "/participants",
                new MessageResponse("Participant joined"));

        return mapParticipantToDTO(saved);
    }

    

    @Transactional
    public void leaveSession(Integer sessionId, Integer userId) {
        LiveParticipant participant = liveParticipantRepository
                .findBySession_SessionIdAndLearner_UserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        participant.setIsCurrentlyConnected(false);
        participant.setLeftAt(LocalDateTime.now());

        if (participant.getJoinedAt() != null) {
            Duration duration = Duration.between(
                    participant.getJoinedAt(),
                    LocalDateTime.now());
            participant.setDurationMinutes(
                    participant.getDurationMinutes() + (int) duration.toMinutes());
        }

        liveParticipantRepository.save(participant);

        // Notify others
        messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId + "/participants",
                new MessageResponse("Participant left"));
    }

    public List<ParticipantDTO> getActiveParticipants(Integer sessionId) {
        return liveParticipantRepository.findActiveParticipants(sessionId)
                .stream()
                .map(this::mapParticipantToDTO)
                .collect(Collectors.toList());
    }


    public LiveSessionDTO getSessionById(Integer sessionId) {
        LiveSession session = liveSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        return convertToDTO(session);
    }

    public List<LiveSessionDTO> getSessionsByCourse(Integer courseId) {
        List<LiveSession> sessions = liveSessionRepository.findByCourse_CourseId(courseId);
        return sessions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<LiveSessionDTO> getUpcomingSessions(Integer creatorUserId) {
        CreatorProfile creator = creatorProfileRepository.findByUser_UserId(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Creator profile not found"));

        List<LiveSession> sessions = liveSessionRepository
                .findByCreator_CreatorIdAndStatusOrderByStartTimeAsc(
                        creator.getCreatorId(), 
                        LiveSession.SessionStatus.SCHEDULED
                );
        
        return sessions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<LiveSessionResponse> getCreatorSessions(Integer creatorId) {
        return liveSessionRepository.findByCreator_CreatorId(creatorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LiveSessionDTO> getLiveSessions() {
        List<LiveSession> sessions = liveSessionRepository
                .findByStatusOrderByActualStartTimeDesc(LiveSession.SessionStatus.LIVE);
        return sessions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private LiveSessionDTO convertToDTO(LiveSession session) {
        int currentParticipants = liveParticipantRepository
                .countBySession_SessionIdAndIsCurrentlyConnected(session.getSessionId(), true);

        List<LiveParticipantDTO> participants = liveParticipantRepository
                .findBySession_SessionId(session.getSessionId())
                .stream()
                .map(this::convertParticipantToDTO)
                .collect(Collectors.toList());

        return LiveSessionDTO.builder()
                .sessionId(session.getSessionId())
                .courseId(session.getCourse().getCourseId())
                .courseTitle(session.getCourse().getTitle())
                .creatorId(session.getCreator().getCreatorId())
                .creatorName(session.getCreator().getUser().getFirstName() + " " + 
                           session.getCreator().getUser().getLastName())
                .creatorProfileImage(session.getCreator().getUser().getProfileImage())
                .title(session.getTitle())
                .description(session.getDescription())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .scheduledDuration(session.getScheduledDuration())
                .roomId(session.getRoomId())
                .meetingLink(session.getMeetingLink())
                .maxParticipants(session.getMaxParticipants())
                .currentParticipants(currentParticipants)
                .status(session.getStatus())
                .actualStartTime(session.getActualStartTime())
                .actualEndTime(session.getActualEndTime())
                .recordingEnabled(session.getRecordingEnabled())
                .autoRecord(session.getAutoRecord())
                .participants(participants)
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    private ParticipantDTO mapParticipantToDTO(LiveParticipant participant) {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setParticipantId(participant.getParticipantId());
        dto.setUserId(participant.getLearner().getUserId());
        dto.setUserName(participant.getLearner().getFirstName() +
                " " + participant.getLearner().getLastName());
        dto.setProfileImage(participant.getLearner().getProfileImage());
        dto.setJoinedAt(participant.getJoinedAt());
        dto.setLeftAt(participant.getLeftAt());
        dto.setDurationMinutes(participant.getDurationMinutes());
        dto.setIsCurrentlyConnected(participant.getIsCurrentlyConnected());
        dto.setConnectionQuality(participant.getConnectionQuality().name());
        dto.setCanSpeak(participant.getCanSpeak());
        dto.setCanVideo(participant.getCanVideo());
        dto.setIsMuted(participant.getIsMuted());
        dto.setVideoDisabled(participant.getVideoDisabled());
        return dto;
    }

    private LiveParticipantDTO convertParticipantToDTO(LiveParticipant participant) {
        return LiveParticipantDTO.builder()
                .participantId(participant.getParticipantId())
                .sessionId(participant.getSession().getSessionId())
                .learnerId(participant.getLearner().getUserId())
                .learnerName(participant.getLearner().getFirstName() + " " + 
                           participant.getLearner().getLastName())
                .learnerEmail(participant.getLearner().getEmail())
                .learnerProfileImage(participant.getLearner().getProfileImage())
                .joinedAt(participant.getJoinedAt())
                .leftAt(participant.getLeftAt())
                .durationMinutes(participant.getDurationMinutes())
                .isCurrentlyConnected(participant.getIsCurrentlyConnected())
                .connectionQuality(participant.getConnectionQuality())
                .canSpeak(participant.getCanSpeak())
                .canVideo(participant.getCanVideo())
                .isMuted(participant.getIsMuted())
                .videoDisabled(participant.getVideoDisabled())
                .build();
    }

        private LiveSessionResponse mapToResponse(LiveSession session) {
        LiveSessionResponse response = new LiveSessionResponse();
        response.setSessionId(session.getSessionId());
        response.setCourseId(session.getCourse().getCourseId());
        response.setCourseTitle(session.getCourse().getTitle());
        response.setCreatorId(session.getCreator().getCreatorId());
        response.setCreatorName(session.getCreator().getUser().getFirstName() +
                " " + session.getCreator().getUser().getLastName());
        response.setTitle(session.getTitle());
        response.setDescription(session.getDescription());
        response.setStartTime(session.getStartTime());
        response.setEndTime(session.getEndTime());
        response.setScheduledDuration(session.getScheduledDuration());
        response.setRoomId(session.getRoomId());
        response.setMeetingLink(session.getMeetingLink());
        response.setMaxParticipants(session.getMaxParticipants());
        response.setStatus(session.getStatus().name());
        response.setActualStartTime(session.getActualStartTime());
        response.setActualEndTime(session.getActualEndTime());
        response.setRecordingEnabled(session.getRecordingEnabled());
        response.setAutoRecord(session.getAutoRecord());
        response.setCurrentParticipants(
                liveParticipantRepository.countActiveParticipants(session.getSessionId()));
        response.setCreatedAt(session.getCreatedAt());
        return response;
    }

}