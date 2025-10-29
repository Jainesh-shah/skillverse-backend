package com.skillverse.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "HobbyCategory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class HobbyCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hobby_id")
    private Integer hobbyId;
    
    @Column(name = "hobby_name", unique = true, nullable = false, length = 100)
    private String hobbyName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
}