package com.skillverse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "SessionRecording")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionRecording {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer recordingId;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private LiveSession session;

    @Column(nullable = false, length = 200)
    private String recordingName;

    @Column(length = 500)
    private String filePath;

    @Column(length = 500)
    private String fileUrl;

    private BigDecimal fileSizeMb;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordingStatus status = RecordingStatus.RECORDING;

    @Column(length = 20)
    private String format = "mp4";

    @Column(length = 20)
    private String resolution;

    private Boolean isPublic = false;
    private Boolean requiresEnrollment = true;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum RecordingStatus {
        RECORDING, PROCESSING, AVAILABLE, FAILED
    }
}