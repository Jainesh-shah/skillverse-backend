package com.skillverse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "WebRTCConnection")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebRTCConnection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer connectionId;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private LiveSession session;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String peerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConnectionType connectionType;

    @Column(columnDefinition = "JSON")
    private String iceCandidates;

    @Column(columnDefinition = "TEXT")
    private String sdpOffer;

    @Column(columnDefinition = "TEXT")
    private String sdpAnswer;

    private Boolean isActive = true;
    private LocalDateTime connectedAt = LocalDateTime.now();
    private LocalDateTime disconnectedAt;

    public enum ConnectionType {
        HOST, PARTICIPANT
    }
}
