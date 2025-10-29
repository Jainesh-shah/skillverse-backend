package com.skillverse.service;

import com.skillverse.dto.SessionControlRequest;
import com.skillverse.dto.WebRTCSignalDTO;
import com.skillverse.exception.ResourceNotFoundException;
import com.skillverse.model.LiveParticipant;
import com.skillverse.model.LiveSession;
import com.skillverse.repository.LiveParticipantRepository;
import com.skillverse.repository.LiveSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SessionControlService {

    @Autowired
    private LiveSessionRepository liveSessionRepository;

    @Autowired
    private LiveParticipantRepository liveParticipantRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void executeControl(SessionControlRequest request, Integer creatorUserId) {
        LiveSession session = liveSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        // Verify creator
        if (!session.getCreator().getUser().getUserId().equals(creatorUserId)) {
            throw new IllegalArgumentException("Only the creator can control the session");
        }

        LiveParticipant participant = liveParticipantRepository
                .findBySession_SessionIdAndLearner_UserId(request.getSessionId(), request.getTargetUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        switch (request.getAction()) {
            case MUTE_AUDIO:
                participant.setIsMuted(true);
                sendControlMessage(session.getRoomId(), request.getTargetUserId(), "mute-audio", null);
                log.info("Muted audio for user {} in session {}", request.getTargetUserId(), request.getSessionId());
                break;

            case UNMUTE_AUDIO:
                participant.setIsMuted(false);
                sendControlMessage(session.getRoomId(), request.getTargetUserId(), "unmute-audio", null);
                log.info("Unmuted audio for user {} in session {}", request.getTargetUserId(), request.getSessionId());
                break;

            case DISABLE_VIDEO:
                participant.setVideoDisabled(true);
                sendControlMessage(session.getRoomId(), request.getTargetUserId(), "disable-video", null);
                log.info("Disabled video for user {} in session {}", request.getTargetUserId(), request.getSessionId());
                break;

            case ENABLE_VIDEO:
                participant.setVideoDisabled(false);
                sendControlMessage(session.getRoomId(), request.getTargetUserId(), "enable-video", null);
                log.info("Enabled video for user {} in session {}", request.getTargetUserId(), request.getSessionId());
                break;

            case KICK_PARTICIPANT:
                participant.setIsCurrentlyConnected(false);
                sendControlMessage(session.getRoomId(), request.getTargetUserId(), "kicked", request.getReason());
                log.info("Kicked user {} from session {}", request.getTargetUserId(), request.getSessionId());
                break;

            case GRANT_SPEAK_PERMISSION:
                participant.setCanSpeak(true);
                sendControlMessage(session.getRoomId(), request.getTargetUserId(), "can-speak", null);
                log.info("Granted speak permission to user {} in session {}", request.getTargetUserId(), request.getSessionId());
                break;

            case REVOKE_SPEAK_PERMISSION:
                participant.setCanSpeak(false);
                participant.setIsMuted(true);
                sendControlMessage(session.getRoomId(), request.getTargetUserId(), "cannot-speak", null);
                log.info("Revoked speak permission from user {} in session {}", request.getTargetUserId(), request.getSessionId());
                break;
        }

        liveParticipantRepository.save(participant);
    }

    private void sendControlMessage(String roomId, Integer targetUserId, String action, String reason) {
        WebRTCSignalDTO controlMessage = WebRTCSignalDTO.builder()
                .type("control")
                .roomId(roomId)
                .targetPeerId(targetUserId.toString())
                .data(new ControlData(action, reason))
                .build();

        messagingTemplate.convertAndSend("/topic/room/" + roomId, controlMessage);
    }

    private static class ControlData {
        public String action;
        public String reason;

        public ControlData(String action, String reason) {
            this.action = action;
            this.reason = reason;
        }
    }
}