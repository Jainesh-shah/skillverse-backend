package com.skillverse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "SessionMessage")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer messageId;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private LiveSession session;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String messageText;

    @Enumerated(EnumType.STRING)
    private MessageType messageType = MessageType.TEXT;

    private LocalDateTime sentAt = LocalDateTime.now();

    public enum MessageType {
        TEXT, SYSTEM, ANNOUNCEMENT
    }
}