package com.skillverse.service;

import com.skillverse.model.Course;
import com.skillverse.model.CourseContent;
import com.skillverse.repository.CourseContentRepository;
import com.skillverse.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseContentService {
    
    @Autowired
    private CourseContentRepository courseContentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Transactional
    public CourseContent addContent(CourseContent content, Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        content.setCourse(course);
        return courseContentRepository.save(content);
    }
    
    public List<CourseContent> getContentByCourse(Integer courseId) {
        return courseContentRepository.findByCourse_CourseId(courseId);
    }
    
    public CourseContent getContentById(Integer contentId) {
        return courseContentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));
    }
    
    @Transactional
    public CourseContent updateContent(Integer contentId, CourseContent updatedContent) {
        CourseContent content = getContentById(contentId);
        
        if (updatedContent.getContentTitle() != null) content.setContentTitle(updatedContent.getContentTitle());
        if (updatedContent.getVideoUrl() != null) content.setVideoUrl(updatedContent.getVideoUrl());
        if (updatedContent.getDuration() != null) content.setDuration(updatedContent.getDuration());
        
        return courseContentRepository.save(content);
    }
    
    @Transactional
    public void deleteContent(Integer contentId) {
        courseContentRepository.deleteById(contentId);
    }
}