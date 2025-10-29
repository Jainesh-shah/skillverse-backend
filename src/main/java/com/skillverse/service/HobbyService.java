package com.skillverse.service;

import com.skillverse.dto.HobbyCategoryDTO;
import com.skillverse.dto.HobbySkillDTO;
import com.skillverse.model.HobbyCategory;
import com.skillverse.model.HobbySkill;
import com.skillverse.repository.HobbyCategoryRepository;
import com.skillverse.repository.HobbySkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HobbyService {

    @Autowired
    private HobbyCategoryRepository hobbyCategoryRepository;

    @Autowired
    private HobbySkillRepository hobbySkillRepository;

    public List<HobbyCategoryDTO> getAllHobbies() {
        return hobbyCategoryRepository.findAll().stream()
                .map(hobby -> new HobbyCategoryDTO(
                        hobby.getHobbyId(),
                        hobby.getHobbyName(),
                        hobby.getDescription()
                ))
                .collect(Collectors.toList());
    }

    public HobbyCategoryDTO getHobbyById(Integer hobbyId) {
        HobbyCategory hobby = hobbyCategoryRepository.findById(hobbyId)
                .orElseThrow(() -> new RuntimeException("Hobby not found"));
        return new HobbyCategoryDTO(
                hobby.getHobbyId(),
                hobby.getHobbyName(),
                hobby.getDescription()
        );
    }

    @Transactional
    public HobbyCategoryDTO createHobby(HobbyCategory hobby) {
        HobbyCategory saved = hobbyCategoryRepository.save(hobby);
        return new HobbyCategoryDTO(
                saved.getHobbyId(),
                saved.getHobbyName(),
                saved.getDescription()
        );
    }

    public List<HobbySkillDTO> getSkillsByHobby(Integer hobbyId) {
        return hobbySkillRepository.findByHobby_HobbyId(hobbyId).stream()
                .map(skill -> new HobbySkillDTO(
                        skill.getSkillId(),
                        skill.getSkillName(),
                        skill.getDescription(),
                        skill.getIsCustom(),
                        skill.getHobby() != null ? skill.getHobby().getHobbyId() : null,
                        skill.getHobby() != null ? skill.getHobby().getHobbyName() : null
                ))
                .collect(Collectors.toList());
    }

    public HobbySkillDTO getSkillById(Integer skillId) {
        HobbySkill skill = hobbySkillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));
        return new HobbySkillDTO(
                skill.getSkillId(),
                skill.getSkillName(),
                skill.getDescription(),
                skill.getIsCustom(),
                skill.getHobby() != null ? skill.getHobby().getHobbyId() : null,
                skill.getHobby() != null ? skill.getHobby().getHobbyName() : null
        );
    }

    @Transactional
    public HobbySkillDTO createSkill(HobbySkill skill) {
        HobbySkill saved = hobbySkillRepository.save(skill);
        return new HobbySkillDTO(
                saved.getSkillId(),
                saved.getSkillName(),
                saved.getDescription(),
                saved.getIsCustom(),
                saved.getHobby() != null ? saved.getHobby().getHobbyId() : null,
                saved.getHobby() != null ? saved.getHobby().getHobbyName() : null
        );
    }
}
