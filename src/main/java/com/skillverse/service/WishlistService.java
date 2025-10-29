package com.skillverse.service;

import com.skillverse.model.Course;
import com.skillverse.model.User;
import com.skillverse.model.Wishlist;
import com.skillverse.repository.CourseRepository;
import com.skillverse.repository.UserRepository;
import com.skillverse.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {
    
    @Autowired
    private WishlistRepository wishlistRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public Wishlist addToWishlist(Integer courseId, Integer learnerId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        User learner = userRepository.findById(learnerId)
                .orElseThrow(() -> new RuntimeException("Learner not found"));
        
        if (wishlistRepository.existsByLearner_UserIdAndCourse_CourseId(learnerId, courseId)) {
            throw new RuntimeException("Course already in wishlist");
        }
        
        Wishlist wishlist = new Wishlist();
        wishlist.setCourse(course);
        wishlist.setLearner(learner);
        
        return wishlistRepository.save(wishlist);
    }
    
    public List<Wishlist> getWishlist(Integer learnerId) {
        return wishlistRepository.findByLearner_UserId(learnerId);
    }
    
    @Transactional
    public void removeFromWishlist(Integer courseId, Integer learnerId) {
        if (!wishlistRepository.existsByLearner_UserIdAndCourse_CourseId(learnerId, courseId)) {
            throw new RuntimeException("Course not in wishlist");
        }
        
        wishlistRepository.deleteByLearner_UserIdAndCourse_CourseId(learnerId, courseId);
    }
}