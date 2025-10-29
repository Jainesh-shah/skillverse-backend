package com.skillverse.dto;

import lombok.Data;

@Data
public class CreatorProfileResponse {
    private Integer creatorId;
    private Integer userId;
    private String firstName;
    private String lastName;
    private String email;
    private Integer experienceYears;
    private String qualification;
    private String location;
    private Boolean verified;
    private String socialLinks;
}