package com.skillverse.dto;

import lombok.Data;

@Data
public class CreatorProfileRequest {
    private Integer experienceYears;
    private String qualification;
    private String location;
    private String socialLinks; // JSON string
}