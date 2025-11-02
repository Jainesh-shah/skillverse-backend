package com.skillverse.repository;

import com.skillverse.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {

    @Query("SELECT c FROM Course c WHERE c.creator.creatorId = :creatorId")
    List<Course> findByCreatorId(@Param("creatorId") Integer creatorId);


    List<Course> findByCreator_CreatorId(Integer creatorId);
    List<Course> findBySkill_SkillId(Integer skillId);
    List<Course> findByCourseType(Course.CourseType courseType);
    List<Course> findByDifficultyLevel(Course.DifficultyLevel difficultyLevel);
    
    @Query("SELECT c FROM Course c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Course> searchCourses(@Param("keyword") String keyword);

    @Query("SELECT c FROM Course c " +
           "WHERE LOWER(c.creator.user.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(c.creator.user.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(c.creator.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Course> searchCoursesByCreator(@Param("keyword") String keyword);
    
}