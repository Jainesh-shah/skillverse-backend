package com.skillverse.service;

import com.skillverse.dto.ReviewRequest;
import com.skillverse.model.Course;
import com.skillverse.model.Review;
import com.skillverse.model.User;
import com.skillverse.repository.CourseRepository;
import com.skillverse.repository.EnrollmentRepository;
import com.skillverse.repository.ReviewRepository;
import com.skillverse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Transactional
    public Review addReview(ReviewRequest request, Integer learnerId) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        User learner = userRepository.findById(learnerId)
                .orElseThrow(() -> new RuntimeException("Learner not found"));
        
        // Check if learner is enrolled
        if (!enrollmentRepository.existsByCourse_CourseIdAndLearner_UserId(
                request.getCourseId(), learnerId)) {
            throw new RuntimeException("Must be enrolled to review this course");
        }
        
        // Check if already reviewed
        if (reviewRepository.existsByCourse_CourseIdAndLearner_UserId(
                request.getCourseId(), learnerId)) {
            throw new RuntimeException("Already reviewed this course");
        }
        
        Review review = new Review();
        review.setCourse(course);
        review.setLearner(learner);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        
        return reviewRepository.save(review);
    }
    
    public List<Review> getCourseReviews(Integer courseId) {
        return reviewRepository.findByCourse_CourseId(courseId);
    }
    
    public Double getCourseAverageRating(Integer courseId) {
        Double avg = reviewRepository.getAverageRatingByCourse(courseId);
        return avg != null ? avg : 0.0;
    }
}