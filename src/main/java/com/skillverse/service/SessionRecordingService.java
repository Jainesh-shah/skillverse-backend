package com.skillverse.service;

import com.skillverse.dto.RecordingAccessDTO;
import com.skillverse.dto.RecordingDTO;
import com.skillverse.exception.ResourceNotFoundException;
import com.skillverse.model.*;
import com.skillverse.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SessionRecordingService {

    @Autowired
    private SessionRecordingRepository recordingRepository;

    @Autowired
    private RecordingAccessRepository recordingAccessRepository;

    @Autowired
    private LiveSessionRepository liveSessionRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${media.server.url:http://localhost:3000}")
    private String mediaServerUrl;

    @Transactional
    public RecordingDTO startRecording(Integer sessionId, Integer creatorUserId) {
        LiveSession session = liveSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getCreator().getUser().getUserId().equals(creatorUserId)) {
            throw new IllegalArgumentException("Only the creator can start recording");
        }

        if (session.getStatus() != LiveSession.SessionStatus.LIVE) {
            throw new IllegalStateException("Session must be live to start recording");
        }

        // Check if already recording
        if (recordingRepository.existsBySession_SessionIdAndStatus(sessionId, SessionRecording.RecordingStatus.RECORDING)) {
            throw new IllegalStateException("Recording already in progress");
        }

        // Create recording record
        SessionRecording recording = new SessionRecording();
        recording.setSession(session);
        recording.setRecordingName(session.getTitle() + " - " + LocalDateTime.now());
        recording.setStartedAt(LocalDateTime.now());
        recording.setStatus(SessionRecording.RecordingStatus.RECORDING);
        recording.setFormat("mp4");
        recording.setResolution("1920x1080");
        recording.setIsPublic(false);
        recording.setRequiresEnrollment(true);

        SessionRecording saved = recordingRepository.save(recording);

        // Send request to media server to start recording
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("roomId", session.getRoomId());
            request.put("recordingId", saved.getRecordingId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            restTemplate.postForEntity(
                mediaServerUrl + "/api/recording/start",
                entity,
                Map.class
            );

            log.info("Started recording {} for session {}", saved.getRecordingId(), sessionId);
        } catch (Exception e) {
            log.error("Failed to start recording on media server", e);
            saved.setStatus(SessionRecording.RecordingStatus.FAILED);
            recordingRepository.save(saved);
            throw new RuntimeException("Failed to start recording: " + e.getMessage());
        }

        return convertToDTO(saved, null);
    }

    @Transactional
    public RecordingDTO stopRecording(Integer sessionId, Integer creatorUserId) {
        LiveSession session = liveSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getCreator().getUser().getUserId().equals(creatorUserId)) {
            throw new IllegalArgumentException("Only the creator can stop recording");
        }

        SessionRecording recording = recordingRepository
                .findBySession_SessionIdAndStatus(sessionId, SessionRecording.RecordingStatus.RECORDING)
                .orElseThrow(() -> new ResourceNotFoundException("No active recording found"));

        recording.setEndedAt(LocalDateTime.now());
        recording.setStatus(SessionRecording.RecordingStatus.PROCESSING);

        if (recording.getStartedAt() != null) {
            long duration = java.time.Duration
                    .between(recording.getStartedAt(), recording.getEndedAt())
                    .toMinutes();
            recording.setDurationMinutes((int) duration);
        }

        SessionRecording updated = recordingRepository.save(recording);

        // Send request to media server to stop recording
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("roomId", session.getRoomId());
            request.put("recordingId", recording.getRecordingId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            restTemplate.postForEntity(
                mediaServerUrl + "/api/recording/stop",
                entity,
                Map.class
            );

            log.info("Stopped recording {} for session {}", recording.getRecordingId(), sessionId);
        } catch (Exception e) {
            log.error("Failed to stop recording on media server", e);
        }

        return convertToDTO(updated, null);
    }

    @Transactional
    public void updateRecordingStatus(Integer recordingId, String filePath, String fileUrl, Long fileSizeBytes) {
        SessionRecording recording = recordingRepository.findById(recordingId)
                .orElseThrow(() -> new ResourceNotFoundException("Recording not found"));

        recording.setFilePath(filePath);
        recording.setFileUrl(fileUrl);
        recording.setFileSizeMb(new java.math.BigDecimal(fileSizeBytes / (1024.0 * 1024.0)));
        recording.setStatus(SessionRecording.RecordingStatus.AVAILABLE);

        recordingRepository.save(recording);

        // Auto-grant access to all enrolled learners
        grantAccessToEnrolledLearners(recording);

        log.info("Updated recording {} status to AVAILABLE", recordingId);
    }

    @Transactional
    public void grantAccessToEnrolledLearners(SessionRecording recording) {
        Integer courseId = recording.getSession().getCourse().getCourseId();
        List<Enrollment> enrollments = enrollmentRepository.findByCourse_CourseId(courseId);

        for (Enrollment enrollment : enrollments) {
            if (!recordingAccessRepository.existsByRecording_RecordingIdAndLearner_UserId(
                    recording.getRecordingId(), enrollment.getLearner().getUserId())) {
                
                RecordingAccess access = new RecordingAccess();
                access.setRecording(recording);
                access.setLearner(enrollment.getLearner());
                access.setGrantedAt(LocalDateTime.now());
                access.setViewCount(0);
                access.setWatchDurationMinutes(0);

                recordingAccessRepository.save(access);
            }
        }

        log.info("Granted access to recording {} for enrolled learners", recording.getRecordingId());
    }

    public List<RecordingDTO> getRecordingsBySession(Integer sessionId, Integer userId) {
        LiveSession session = liveSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        boolean isCreator = session.getCreator().getUser().getUserId().equals(userId);

        List<SessionRecording> recordings = recordingRepository.findBySession_SessionId(sessionId);

        return recordings.stream()
                .map(r -> convertToDTO(r, isCreator ? null : userId))
                .collect(Collectors.toList());
    }

    public RecordingDTO getRecordingById(Integer recordingId, Integer userId) {
        SessionRecording recording = recordingRepository.findById(recordingId)
                .orElseThrow(() -> new ResourceNotFoundException("Recording not found"));

        boolean isCreator = recording.getSession().getCreator().getUser().getUserId().equals(userId);

        if (!isCreator) {
            // Check access
            boolean hasAccess = recordingAccessRepository
                    .existsByRecording_RecordingIdAndLearner_UserId(recordingId, userId);
            if (!hasAccess && recording.getRequiresEnrollment()) {
                throw new IllegalArgumentException("You don't have access to this recording");
            }
        }

        return convertToDTO(recording, isCreator ? null : userId);
    }

    @Transactional
    public void trackView(Integer recordingId, Integer userId, Integer watchDuration) {
        RecordingAccess access = recordingAccessRepository
                .findByRecording_RecordingIdAndLearner_UserId(recordingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recording access not found"));

        access.setLastViewedAt(LocalDateTime.now());
        access.setViewCount(access.getViewCount() + 1);
        access.setWatchDurationMinutes(access.getWatchDurationMinutes() + watchDuration);

        recordingAccessRepository.save(access);
        log.info("Tracked view for recording {} by user {}", recordingId, userId);
    }

    private RecordingDTO convertToDTO(SessionRecording recording, Integer learnerId) {
        Boolean hasAccess = null;
        if (learnerId != null) {
            hasAccess = recordingAccessRepository
                    .existsByRecording_RecordingIdAndLearner_UserId(recording.getRecordingId(), learnerId);
        }

        return RecordingDTO.builder()
                .recordingId(recording.getRecordingId())
                .sessionId(recording.getSession().getSessionId())
                .sessionTitle(recording.getSession().getTitle())
                .courseTitle(recording.getSession().getCourse().getTitle())
                .creatorName(recording.getSession().getCreator().getUser().getFirstName() + " " +
                           recording.getSession().getCreator().getUser().getLastName())
                .recordingName(recording.getRecordingName())
                .filePath(recording.getFilePath())
                .fileUrl(recording.getFileUrl())
                .fileSizeMb(recording.getFileSizeMb())
                .startedAt(recording.getStartedAt())
                .endedAt(recording.getEndedAt())
                .durationMinutes(recording.getDurationMinutes())
                .status(recording.getStatus())
                .format(recording.getFormat())
                .resolution(recording.getResolution())
                .isPublic(recording.getIsPublic())
                .requiresEnrollment(recording.getRequiresEnrollment())
                .hasAccess(hasAccess)
                .createdAt(recording.getCreatedAt())
                .updatedAt(recording.getUpdatedAt())
                .build();
    }
}