package com.skillverse.repository;

import com.skillverse.model.HobbySkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HobbySkillRepository extends JpaRepository<HobbySkill, Integer> {
    List<HobbySkill> findByHobby_HobbyId(Integer hobbyId);
}