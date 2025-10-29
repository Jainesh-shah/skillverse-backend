package com.skillverse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Course")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Integer courseId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private CreatorProfile creator;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    private HobbySkill skill;
    
    @Column(nullable = false, length = 150)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", nullable = false)
    private CourseType courseType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel;
    
    @Column(length = 50)
    private String duration;
    
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public enum CourseType {
        Recorded, Live, Hybrid
    }
    
    public enum DifficultyLevel {
        Beginner, Intermediate, Advanced
    }
}