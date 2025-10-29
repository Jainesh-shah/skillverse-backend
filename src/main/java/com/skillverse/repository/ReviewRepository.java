package com.skillverse.repository;

import com.skillverse.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByCourse_CourseId(Integer courseId);
    boolean existsByCourse_CourseIdAndLearner_UserId(Integer courseId, Integer learnerId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.courseId = :courseId")
    Double getAverageRatingByCourse(@Param("courseId") Integer courseId);
}