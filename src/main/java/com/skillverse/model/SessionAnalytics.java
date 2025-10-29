package com.skillverse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "SessionAnalytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionAnalytics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer analyticsId;

    @OneToOne
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private LiveSession session;

    private Integer totalParticipants = 0;
    private Integer peakConcurrentUsers = 0;
    private BigDecimal averageDurationMinutes;

    private Integer totalMessages = 0;
    private Integer questionsAsked = 0;

    @Column(length = 20)
    private String averageConnectionQuality;

    private Integer totalDisconnections = 0;
    private Integer recordingViews = 0;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}