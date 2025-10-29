package com.skillverse.repository;

import com.skillverse.model.CreatorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CreatorProfileRepository extends JpaRepository<CreatorProfile, Integer> {
    Optional<CreatorProfile> findByUser_UserId(Integer userId);
}