package com.skillverse.service;

import com.skillverse.dto.CreatorDetailDTO;
import com.skillverse.dto.CreatorPublicDTO;
import com.skillverse.dto.CourseResponse;
import com.skillverse.model.Course;
import com.skillverse.model.CreatorProfile;
import com.skillverse.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CreatorPublicService {
    
    @Autowired
    private CreatorProfileRepository creatorProfileRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private CourseService courseService;
    
    public List<CreatorPublicDTO> getAllCreators() {
        return creatorProfileRepository.findAll().stream()
                .map(this::convertToPublicDTO)
                .collect(Collectors.toList());
    }
    
    public List<CreatorPublicDTO> searchCreators(String keyword) {
        return creatorProfileRepository.searchCreators(keyword).stream()
                .map(this::convertToPublicDTO)
                .collect(Collectors.toList());
    }
    
    public List<CreatorPublicDTO> getCreatorsBySkill(Integer skillId) {
        return creatorProfileRepository.findCreatorsBySkill(skillId).stream()
                .map(this::convertToPublicDTO)
                .collect(Collectors.toList());
    }
    
    public List<CreatorPublicDTO> getCreatorsBySkills(List<Integer> skillIds) {
    if (skillIds == null || skillIds.isEmpty()) {
        return getAllCreators();
    }
    
    // Get all creators
    List<CreatorProfile> allCreators = creatorProfileRepository.findAll();
    
    // Filter creators who have ANY of the specified skills (OR logic)
    return allCreators.stream()
            .filter(creator -> {
                // Get all skill IDs for this creator from their courses
                List<Course> creatorCourses = courseRepository.findByCreator_CreatorId(creator.getCreatorId());
                Set<Integer> creatorSkillIds = creatorCourses.stream()
                        .filter(course -> course.getSkill() != null)
                        .map(course -> course.getSkill().getSkillId())
                        .collect(Collectors.toSet());
                
                // Check if creator has ANY of the requested skills (OR logic)
                return skillIds.stream().anyMatch(creatorSkillIds::contains);
            })
            .map(this::convertToPublicDTO)
            .collect(Collectors.toList());
}
    public CreatorDetailDTO getCreatorDetail(Integer creatorId) {
        CreatorProfile creator = creatorProfileRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));
        
        return convertToDetailDTO(creator);
    }
    
    private CreatorPublicDTO convertToPublicDTO(CreatorProfile creator) {
        CreatorPublicDTO dto = new CreatorPublicDTO();
        dto.setCreatorId(creator.getCreatorId());
        dto.setUserId(creator.getUser().getUserId());
        dto.setFirstName(creator.getUser().getFirstName());
        dto.setLastName(creator.getUser().getLastName());
        dto.setEmail(creator.getUser().getEmail());
        dto.setBio(creator.getUser().getBio());
        dto.setExperienceYears(creator.getExperienceYears());
        dto.setQualification(creator.getQualification());
        dto.setLocation(creator.getLocation());
        dto.setVerified(creator.getVerified());
        dto.setSocialLinks(creator.getSocialLinks());
        
        // Get courses count
        List<Course> courses = courseRepository.findByCreator_CreatorId(creator.getCreatorId());
        dto.setTotalCourses(courses.size());
        
        // Get total students (unique enrollments across all courses)
        Integer totalStudents = enrollmentRepository.countDistinctLearnersByCreatorId(creator.getCreatorId());
        dto.setTotalStudents(totalStudents != null ? totalStudents : 0);
        
        // Calculate average rating across all courses
        Double avgRating = reviewRepository.getAverageRatingByCreator(creator.getCreatorId());
        dto.setAverageRating(avgRating != null ? avgRating : 0.0);
        
        // Get unique skills from creator's courses
        List<String> skills = courses.stream()
                .filter(c -> c.getSkill() != null)
                .map(c -> c.getSkill().getSkillName())
                .distinct()
                .collect(Collectors.toList());
        dto.setSkills(skills);
        
        return dto;
    }
    
    private CreatorDetailDTO convertToDetailDTO(CreatorProfile creator) {
        CreatorDetailDTO dto = new CreatorDetailDTO();
        dto.setCreatorId(creator.getCreatorId());
        dto.setUserId(creator.getUser().getUserId());
        dto.setFirstName(creator.getUser().getFirstName());
        dto.setLastName(creator.getUser().getLastName());
        dto.setEmail(creator.getUser().getEmail());
        dto.setBio(creator.getUser().getBio());
        dto.setExperienceYears(creator.getExperienceYears());
        dto.setQualification(creator.getQualification());
        dto.setLocation(creator.getLocation());
        dto.setVerified(creator.getVerified());
        dto.setSocialLinks(creator.getSocialLinks());
        
        // Get courses
        List<Course> courses = courseRepository.findByCreator_CreatorId(creator.getCreatorId());
        dto.setTotalCourses(courses.size());
        
        // Convert courses to CourseResponse
        List<CourseResponse> courseResponses = courses.stream()
                .map(course -> courseService.getCourseById(course.getCourseId()))
                .collect(Collectors.toList());
        dto.setCourses(courseResponses);
        
        // Get total students
        Integer totalStudents = enrollmentRepository.countDistinctLearnersByCreatorId(creator.getCreatorId());
        dto.setTotalStudents(totalStudents != null ? totalStudents : 0);
        
        // Calculate average rating
        Double avgRating = reviewRepository.getAverageRatingByCreator(creator.getCreatorId());
        dto.setAverageRating(avgRating != null ? avgRating : 0.0);
        
        // Get unique skills
        List<String> skills = courses.stream()
                .filter(c -> c.getSkill() != null)
                .map(c -> c.getSkill().getSkillName())
                .distinct()
                .collect(Collectors.toList());
        dto.setSkills(skills);
        
        return dto;
    }
}