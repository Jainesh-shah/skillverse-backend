package com.skillverse.controller;

import com.skillverse.dto.EnrollmentDTO;
import com.skillverse.dto.EnrollmentRequest;
import com.skillverse.dto.MessageResponse;
import com.skillverse.service.EnrollmentService;
import com.skillverse.security.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
@CrossOrigin(origins = "*")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @PostMapping
    @PreAuthorize("hasRole('Learner')")
    public ResponseEntity<?> enrollInCourse(@Valid @RequestBody EnrollmentRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            EnrollmentDTO enrollmentDTO = enrollmentService.enrollInCourse(request.getCourseId(), userId);
            return ResponseEntity.ok(enrollmentDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/learner/course/{courseId}")
    @PreAuthorize("hasRole('Learner')")
    public ResponseEntity<EnrollmentDTO> getEnrollmentForLearner(
            @PathVariable Integer courseId,
            Authentication authentication) {

        String email = authentication.getName();
        Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();

        EnrollmentDTO enrollment = enrollmentService.getEnrollmentForLearner(courseId, userId);
        return ResponseEntity.ok(enrollment);
    }

    @GetMapping("/my-enrollments")
    @PreAuthorize("hasRole('Learner')")
    public ResponseEntity<List<EnrollmentDTO>> getMyEnrollments(Authentication authentication) {
        String email = authentication.getName();
        Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
        List<EnrollmentDTO> dtoList = enrollmentService.getEnrollmentsByLearner(userId);
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<List<EnrollmentDTO>> getCourseEnrollments(@PathVariable Integer courseId) {
        List<EnrollmentDTO> dtoList = enrollmentService.getEnrollmentsByCourse(courseId);
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/check/{courseId}")
    public ResponseEntity<Boolean> checkEnrollment(@PathVariable Integer courseId,
            Authentication authentication) {
        String email = authentication.getName();
        Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
        boolean enrolled = enrollmentService.isEnrolled(courseId, userId);
        return ResponseEntity.ok(enrolled);
    }
}
