package com.skillverse.service;

import com.skillverse.dto.JoinSessionResponse;
import com.skillverse.dto.LiveParticipantDTO;
import com.skillverse.dto.LiveSessionDTO;
import com.skillverse.dto.MediaServerConfigDTO;
import com.skillverse.exception.ResourceNotFoundException;
import com.skillverse.model.*;
import com.skillverse.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LiveParticipantService {

    @Autowired
    private LiveParticipantRepository liveParticipantRepository;

    @Autowired
    private LiveSessionRepository liveSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LiveSessionService liveSessionService;

    @Value("${websocket.url:ws://localhost:8080/ws/signaling}")
    private String wsUrl;

    @Value("${media.server.url:http://localhost:3000}")
    private String mediaServerUrl;

    @Transactional
    public synchronized JoinSessionResponse joinSession(Integer sessionId, Integer userId) {
        LiveSession session = liveSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isCreator = session.getCreator().getUser().getUserId().equals(userId);

        // ✅ Check session status
        if (session.getStatus() != LiveSession.SessionStatus.LIVE &&
            session.getStatus() != LiveSession.SessionStatus.SCHEDULED) {
            throw new IllegalStateException("Session has ended or been cancelled");
        }

        // ✅ For learners, verify enrollment
        if (!isCreator) {
            boolean isEnrolled = enrollmentRepository
                    .existsByCourse_CourseIdAndLearner_UserId(session.getCourse().getCourseId(), userId);
            if (!isEnrolled) {
                throw new IllegalArgumentException("You must be enrolled in the course to join the session");
            }
        }

        // ✅ Check participant limit only for LIVE status
        if (session.getStatus() == LiveSession.SessionStatus.LIVE && !isCreator) {
            int currentCount = liveParticipantRepository
                    .countBySession_SessionIdAndIsCurrentlyConnected(sessionId, true);
            if (currentCount >= session.getMaxParticipants()) {
                throw new IllegalStateException("Session is full");
            }
        }

        // ✅ Find existing participant record safely
        Optional<LiveParticipant> existingOpt = liveParticipantRepository
                .findBySession_SessionIdAndLearner_UserId(sessionId, userId);

        LiveParticipant participant;
        if (existingOpt.isPresent()) {
            // ✅ Reuse existing record
            participant = existingOpt.get();
            participant.setLeftAt(null);
            participant.setDurationMinutes(null);
            participant.setIsCurrentlyConnected(true);
            participant.setJoinedAt(LocalDateTime.now());
            log.info("User {} rejoined existing session record {}", userId, sessionId);
        } else {
            // ✅ Create new record only if not exists
            participant = new LiveParticipant();
            participant.setSession(session);
            participant.setLearner(user);
            participant.setCanSpeak(true);
            participant.setCanVideo(true);
            participant.setIsMuted(false);
            participant.setVideoDisabled(false);
            participant.setIsCurrentlyConnected(true);
            participant.setJoinedAt(LocalDateTime.now());
            participant.setConnectionQuality(LiveParticipant.ConnectionQuality.GOOD);
            log.info("User {} joined new session record {}", userId, sessionId);
        }

        LiveParticipant saved = liveParticipantRepository.save(participant);

        // ✅ Fetch other active participants
        List<LiveParticipantDTO> existingParticipants =
                liveParticipantRepository.findBySession_SessionIdAndIsCurrentlyConnected(sessionId, true)
                        .stream()
                        .filter(p -> !p.getLearner().getUserId().equals(userId))
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());

        // ✅ Build response payload
        LiveSessionDTO sessionDTO = liveSessionService.getSessionById(sessionId);
        LiveParticipantDTO participantDTO = convertToDTO(saved);
        MediaServerConfigDTO mediaConfig = buildMediaConfig(session.getRoomId());
        String sessionToken = generateSessionToken(sessionId, userId);

        return JoinSessionResponse.builder()
                .session(sessionDTO)
                .participant(participantDTO)
                .mediaConfig(mediaConfig)
                .wsUrl(wsUrl)
                .existingParticipants(existingParticipants)
                .isCreator(isCreator)
                .token(sessionToken)
                .build();
    }

    @Transactional
    public void leaveSession(Integer sessionId, Integer userId) {
        LiveParticipant participant = liveParticipantRepository
                .findBySession_SessionIdAndLearner_UserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        participant.setIsCurrentlyConnected(false);
        participant.setLeftAt(LocalDateTime.now());

        if (participant.getJoinedAt() != null) {
            long duration = java.time.Duration
                    .between(participant.getJoinedAt(), participant.getLeftAt())
                    .toMinutes();
            participant.setDurationMinutes((int) duration);
        }

        liveParticipantRepository.save(participant);
        log.info("User {} left session {}", userId, sessionId);
    }

    public List<LiveParticipantDTO> getSessionParticipants(Integer sessionId) {
        return liveParticipantRepository
                .findBySession_SessionIdAndIsCurrentlyConnected(sessionId, true)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private LiveParticipantDTO convertToDTO(LiveParticipant participant) {
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

    private MediaServerConfigDTO buildMediaConfig(String roomId) {
        List<MediaServerConfigDTO.IceServer> iceServers = List.of(
                MediaServerConfigDTO.IceServer.builder()
                        .urls(List.of("stun:stun.l.google.com:19302"))
                        .build(),
                MediaServerConfigDTO.IceServer.builder()
                        .urls(List.of("turn:your-turn-server.com:3478"))
                        .username("turnuser")
                        .credential("turnpassword")
                        .build()
        );

        return MediaServerConfigDTO.builder()
                .roomId(roomId)
                .mediaServerUrl(mediaServerUrl)
                .iceServers(iceServers)
                .build();
    }

    private String generateSessionToken(Integer sessionId, Integer userId) {
        return UUID.randomUUID().toString();
    }
}
