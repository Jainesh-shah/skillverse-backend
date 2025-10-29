package com.skillverse.controller;

import com.skillverse.dto.MessageResponse;
import com.skillverse.dto.ReviewRequest;
import com.skillverse.model.Review;
import com.skillverse.security.UserDetailsServiceImpl;
import com.skillverse.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @PostMapping
    @PreAuthorize("hasRole('Learner')")
    public ResponseEntity<?> addReview(@Valid @RequestBody ReviewRequest request,
                                       Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            Review review = reviewService.addReview(request, userId);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Review>> getCourseReviews(@PathVariable Integer courseId) {
        return ResponseEntity.ok(reviewService.getCourseReviews(courseId));
    }
    
    @GetMapping("/course/{courseId}/average")
    public ResponseEntity<Double> getCourseAverageRating(@PathVariable Integer courseId) {
        return ResponseEntity.ok(reviewService.getCourseAverageRating(courseId));
    }
}