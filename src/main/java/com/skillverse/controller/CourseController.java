package com.skillverse.controller;

import com.skillverse.dto.CourseRequest;
import com.skillverse.dto.CourseResponse;
import com.skillverse.dto.MessageResponse;
import com.skillverse.model.Course;
import com.skillverse.model.CreatorProfile;
import com.skillverse.repository.CreatorProfileRepository;
import com.skillverse.security.UserDetailsServiceImpl;
import com.skillverse.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private CreatorProfileRepository creatorProfileRepository;

    // Public endpoints
    @GetMapping("/public/all")
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Integer id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @GetMapping("/public/search")
    public ResponseEntity<List<CourseResponse>> searchCourses(@RequestParam String keyword) {
        return ResponseEntity.ok(courseService.searchCourses(keyword));
    }

    @GetMapping("/public/skill/{skillId}")
    public ResponseEntity<List<CourseResponse>> getCoursesBySkill(@PathVariable Integer skillId) {
        return ResponseEntity.ok(courseService.getCoursesBySkill(skillId));
    }

    @GetMapping("/public/type/{type}")
    public ResponseEntity<List<CourseResponse>> getCoursesByType(@PathVariable String type) {
        return ResponseEntity.ok(courseService.getCoursesByType(type));
    }

    @GetMapping("/public/difficulty/{difficulty}")
    public ResponseEntity<List<CourseResponse>> getCoursesByDifficulty(@PathVariable String difficulty) {
        return ResponseEntity.ok(courseService.getCoursesByDifficulty(difficulty));
    }

    // Protected endpoints
    @PostMapping
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<?> createCourse(@Valid @RequestBody CourseRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            Course course = courseService.createCourse(request, userId);
            return ResponseEntity.ok(course);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<List<CourseResponse>> getCoursesByCreator(@PathVariable Integer creatorId) {
        return ResponseEntity.ok(courseService.getCoursesByCreator(creatorId));
    }

    @GetMapping("/my-courses")
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<List<CourseResponse>> getMyCourses(Authentication authentication) {
        String email = authentication.getName();
        Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();

        // Get the creator profile ID using the userId
        CreatorProfile creator = creatorProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Creator profile not found"));

                System.out.println("Creator ID: " + creator.getCreatorId());

        List<CourseResponse> myCourses = courseService.getCoursesByCreator(creator.getCreatorId());
        return ResponseEntity.ok(myCourses);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<?> updateCourse(@PathVariable Integer id,
            @Valid @RequestBody CourseRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            Course course = courseService.updateCourse(id, request, userId);
            return ResponseEntity.ok(course);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<?> deleteCourse(@PathVariable Integer id, Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            courseService.deleteCourse(id, userId);
            return ResponseEntity.ok(new MessageResponse("Course deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/search/creator")
public ResponseEntity<List<CourseResponse>> searchCoursesByCreator(@RequestParam String keyword) {
    return ResponseEntity.ok(courseService.searchCoursesByCreator(keyword));
}

}