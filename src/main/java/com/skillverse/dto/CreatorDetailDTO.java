package com.skillverse.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreatorDetailDTO {
    private Integer creatorId;
    private Integer userId;
    private String firstName;
    private String lastName;
    private String email;
    private String bio;
    private Integer experienceYears;
    private String qualification;
    private String location;
    private Boolean verified;
    private String socialLinks;
    private Integer totalCourses;
    private Integer totalStudents;
    private Double averageRating;
    private List<String> skills;
    private List<CourseResponse> courses;
}