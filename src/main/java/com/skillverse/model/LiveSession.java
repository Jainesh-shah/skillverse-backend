package com.skillverse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "LiveSession")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer sessionId;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private CreatorProfile creator;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer scheduledDuration; // in minutes

    @Column(unique = true, nullable = false, length = 100)
    private String roomId;

    @Column(length = 500)
    private String meetingLink;

    private Integer maxParticipants = 50;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.SCHEDULED;

    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;

    private Boolean recordingEnabled = false;
    private Boolean autoRecord = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum SessionStatus {
        SCHEDULED, LIVE, COMPLETED, CANCELLED
    }
}