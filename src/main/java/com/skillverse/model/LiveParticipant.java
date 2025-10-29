package com.skillverse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "LiveParticipant", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"session_id", "learner_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer participantId;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private LiveSession session;

    @ManyToOne
    @JoinColumn(name = "learner_id", nullable = false)
    private User learner;

    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private Integer durationMinutes = 0;

    private Boolean isCurrentlyConnected = false;

    @Enumerated(EnumType.STRING)
    private ConnectionQuality connectionQuality = ConnectionQuality.GOOD;

    private Boolean canSpeak = true;
    private Boolean canVideo = true;
    private Boolean isMuted = false;
    private Boolean videoDisabled = false;

    public enum ConnectionQuality {
        EXCELLENT, GOOD, FAIR, POOR
    }
}