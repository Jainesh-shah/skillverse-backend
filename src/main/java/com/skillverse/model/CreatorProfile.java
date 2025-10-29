package com.skillverse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CreatorProfile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatorProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "creator_id")
    private Integer creatorId;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "experience_years")
    private Integer experienceYears;
    
    @Column(length = 255)
    private String qualification;
    
    @Column(length = 150)
    private String location;
    
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean verified = false;
    
    @Column(name = "social_links", columnDefinition = "JSON")
    private String socialLinks;

}