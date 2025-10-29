package com.skillverse.repository;

import com.skillverse.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
    List<Wishlist> findByLearner_UserId(Integer learnerId);
    boolean existsByLearner_UserIdAndCourse_CourseId(Integer learnerId, Integer courseId);
    void deleteByLearner_UserIdAndCourse_CourseId(Integer learnerId, Integer courseId);
}