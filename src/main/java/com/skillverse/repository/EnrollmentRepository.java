package com.skillverse.repository;

import com.skillverse.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {
    List<Enrollment> findByLearner_UserId(Integer learnerId);
    List<Enrollment> findByCourse_CourseId(Integer courseId);
    Optional<Enrollment> findByCourse_CourseIdAndLearner_UserId(Integer courseId, Integer learnerId);
    boolean existsByCourse_CourseIdAndLearner_UserId(Integer courseId, Integer learnerId);
}
