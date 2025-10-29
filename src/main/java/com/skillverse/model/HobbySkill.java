package com.skillverse.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "HobbySkill")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class HobbySkill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_id")
    private Integer skillId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hobby_id")
    private HobbyCategory hobby;
    
    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;
    
    @Column(name = "is_custom")
    private Boolean isCustom = false;
    
    @Column(columnDefinition = "TEXT")
    private String description;
}