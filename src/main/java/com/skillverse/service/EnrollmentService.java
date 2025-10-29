package com.skillverse.service;

import com.skillverse.dto.EnrollmentDTO;
import com.skillverse.model.Course;
import com.skillverse.model.Enrollment;
import com.skillverse.model.User;
import com.skillverse.repository.CourseRepository;
import com.skillverse.repository.EnrollmentRepository;
import com.skillverse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public EnrollmentDTO enrollInCourse(Integer courseId, Integer learnerId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        User learner = userRepository.findById(learnerId)
                .orElseThrow(() -> new RuntimeException("Learner not found"));

        if (enrollmentRepository.existsByCourse_CourseIdAndLearner_UserId(courseId, learnerId)) {
            throw new RuntimeException("Already enrolled in this course");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setLearner(learner);
        if (course.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            enrollment.setPaymentStatus(Enrollment.PaymentStatus.Completed);
        } else {
            enrollment.setPaymentStatus(Enrollment.PaymentStatus.Pending);
        }

        Enrollment saved = enrollmentRepository.save(enrollment);
        return mapToDTO(saved);
    }

    public List<EnrollmentDTO> getEnrollmentsByLearner(Integer learnerId) {
        return enrollmentRepository.findByLearner_UserId(learnerId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EnrollmentDTO> getEnrollmentsByCourse(Integer courseId) {
        return enrollmentRepository.findByCourse_CourseId(courseId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public boolean isEnrolled(Integer courseId, Integer learnerId) {
        return enrollmentRepository.existsByCourse_CourseIdAndLearner_UserId(courseId, learnerId);
    }

    @Transactional
    public EnrollmentDTO updatePaymentStatus(Integer enrollId, Enrollment.PaymentStatus status) {
        Enrollment enrollment = enrollmentRepository.findById(enrollId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        enrollment.setPaymentStatus(status);
        Enrollment saved = enrollmentRepository.save(enrollment);
        return mapToDTO(saved);
    }

    public EnrollmentDTO getEnrollmentForLearner(Integer courseId, Integer userId) {
        return enrollmentRepository.findByCourse_CourseIdAndLearner_UserId(courseId, userId)
                .map(this::mapToDTO)
                .orElse(null);
    }

    private EnrollmentDTO mapToDTO(Enrollment enrollment) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setEnrollmentId(enrollment.getEnrollId());
        dto.setCourseId(enrollment.getCourse().getCourseId());
        dto.setCourseTitle(enrollment.getCourse().getTitle());
        dto.setCourseDescription(enrollment.getCourse().getDescription());
        dto.setCourseThumbnail(enrollment.getCourse().getThumbnailUrl());

        User creator = enrollment.getCourse().getCreator().getUser();
        dto.setCreatorName(creator.getFirstName() + " " + creator.getLastName());
        dto.setCreatorId(enrollment.getCourse().getCreator().getCreatorId());

        dto.setCoursePrice(enrollment.getCourse().getPrice());
        dto.setPaymentStatus(enrollment.getPaymentStatus().name());
        dto.setEnrolledAt(enrollment.getEnrolledAt());

        return dto;
    }
}
