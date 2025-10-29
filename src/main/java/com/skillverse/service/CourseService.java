package com.skillverse.service;

import com.skillverse.dto.CourseRequest;
import com.skillverse.dto.CourseResponse;
import com.skillverse.model.*;
import com.skillverse.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private CreatorProfileRepository creatorProfileRepository;
    
    @Autowired
    private HobbySkillRepository hobbySkillRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Transactional
    public Course createCourse(CourseRequest request, Integer userId) {
        CreatorProfile creator = creatorProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Creator profile not found"));
        
        HobbySkill skill = hobbySkillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new RuntimeException("Skill not found"));
        
        Course course = new Course();
        course.setCreator(creator);
        course.setSkill(skill);
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setPrice(request.getPrice() != null ? request.getPrice() : BigDecimal.ZERO);
        course.setCourseType(Course.CourseType.valueOf(request.getCourseType()));
        
        if (request.getDifficultyLevel() != null) {
            course.setDifficultyLevel(Course.DifficultyLevel.valueOf(request.getDifficultyLevel()));
        }
        
        course.setDuration(request.getDuration());
        course.setThumbnailUrl(request.getThumbnailUrl());
        
        return courseRepository.save(course);
    }
    
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<CourseResponse> getCoursesByCreator(Integer creatorId) {
    return courseRepository.findByCreatorId(creatorId).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
}

    
    public List<CourseResponse> searchCourses(String keyword) {
        return courseRepository.searchCourses(keyword).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<CourseResponse> getCoursesBySkill(Integer skillId) {
        return courseRepository.findBySkill_SkillId(skillId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<CourseResponse> getCoursesByType(String courseType) {
        return courseRepository.findByCourseType(Course.CourseType.valueOf(courseType)).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<CourseResponse> getCoursesByDifficulty(String difficulty) {
        return courseRepository.findByDifficultyLevel(Course.DifficultyLevel.valueOf(difficulty)).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public CourseResponse getCourseById(Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return convertToResponse(course);
    }
    
    @Transactional
    public Course updateCourse(Integer courseId, CourseRequest request, Integer userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        if (!course.getCreator().getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this course");
        }
        
        if (request.getSkillId() != null) {
            HobbySkill skill = hobbySkillRepository.findById(request.getSkillId())
                    .orElseThrow(() -> new RuntimeException("Skill not found"));
            course.setSkill(skill);
        }
        
        if (request.getTitle() != null) course.setTitle(request.getTitle());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getPrice() != null) course.setPrice(request.getPrice());
        if (request.getCourseType() != null) course.setCourseType(Course.CourseType.valueOf(request.getCourseType()));
        if (request.getDifficultyLevel() != null) course.setDifficultyLevel(Course.DifficultyLevel.valueOf(request.getDifficultyLevel()));
        if (request.getDuration() != null) course.setDuration(request.getDuration());
        if (request.getThumbnailUrl() != null) course.setThumbnailUrl(request.getThumbnailUrl());
        
        return courseRepository.save(course);
    }
    
    @Transactional
    public void deleteCourse(Integer courseId, Integer userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        if (!course.getCreator().getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this course");
        }
        
        courseRepository.delete(course);
    }

    
    
    private CourseResponse convertToResponse(Course course) {
        CourseResponse response = new CourseResponse();
        response.setCourseId(course.getCourseId());
        response.setCreatorId(course.getCreator().getCreatorId());
        response.setCreatorName(course.getCreator().getUser().getFirstName() + " " + 
                                course.getCreator().getUser().getLastName());
        
        if (course.getSkill() != null) {
            response.setSkillId(course.getSkill().getSkillId());
            response.setSkillName(course.getSkill().getSkillName());
        }
        
        response.setTitle(course.getTitle());
        response.setDescription(course.getDescription());
        response.setPrice(course.getPrice());
        response.setCourseType(course.getCourseType().name());
        
        if (course.getDifficultyLevel() != null) {
            response.setDifficultyLevel(course.getDifficultyLevel().name());
        }
        
        response.setDuration(course.getDuration());
        response.setThumbnailUrl(course.getThumbnailUrl());
        response.setCreatedAt(course.getCreatedAt());
        
        // Get average rating
        Double avgRating = reviewRepository.getAverageRatingByCourse(course.getCourseId());
        response.setAverageRating(avgRating != null ? avgRating : 0.0);
        
        // Get total enrollments
        Integer totalEnrollments = enrollmentRepository.findByCourse_CourseId(course.getCourseId()).size();
        response.setTotalEnrollments(totalEnrollments);
        
        return response;
    }
}