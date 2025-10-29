package com.skillverse.repository;

import com.skillverse.model.HobbyCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HobbyCategoryRepository extends JpaRepository<HobbyCategory, Integer> {
    Optional<HobbyCategory> findByHobbyName(String hobbyName);
}