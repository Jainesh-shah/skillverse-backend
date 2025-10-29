package com.skillverse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CourseRequest {
    @NotNull
    private Integer skillId;
    
    @NotBlank
    private String title;
    
    @NotBlank
    private String description;
    
    private BigDecimal price;
    
    @NotBlank
    private String courseType; // "Recorded", "Live", "Hybrid"
    
    private String difficultyLevel; // "Beginner", "Intermediate", "Advanced"
    
    private String duration;
    
    private String thumbnailUrl;
}