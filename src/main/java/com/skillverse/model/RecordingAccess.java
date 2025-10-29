package com.skillverse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "RecordingAccess", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"recording_id", "learner_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordingAccess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer accessId;

    @ManyToOne
    @JoinColumn(name = "recording_id", nullable = false)
    private SessionRecording recording;

    @ManyToOne
    @JoinColumn(name = "learner_id", nullable = false)
    private User learner;

    private LocalDateTime grantedAt = LocalDateTime.now();
    private LocalDateTime lastViewedAt;
    private Integer viewCount = 0;
    private Integer watchDurationMinutes = 0;
}