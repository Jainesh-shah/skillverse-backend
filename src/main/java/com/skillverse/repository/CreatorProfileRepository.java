package com.skillverse.repository;

import com.skillverse.model.CreatorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreatorProfileRepository extends JpaRepository<CreatorProfile, Integer> {
    Optional<CreatorProfile> findByUser_UserId(Integer userId);

    @Query("SELECT cp FROM CreatorProfile cp WHERE " +
           "LOWER(cp.user.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cp.user.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cp.user.bio) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cp.qualification) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cp.location) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<CreatorProfile> searchCreators(@Param("keyword") String keyword);
    
    @Query("SELECT DISTINCT c.creator FROM Course c WHERE c.skill.skillId = :skillId")
    List<CreatorProfile> findCreatorsBySkill(@Param("skillId") Integer skillId);
}