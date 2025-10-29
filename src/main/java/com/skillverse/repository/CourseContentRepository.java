package com.skillverse.repository;

import com.skillverse.model.CourseContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseContentRepository extends JpaRepository<CourseContent, Integer> {
    List<CourseContent> findByCourse_CourseId(Integer courseId);
}