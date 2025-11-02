package com.skillverse.controller;

import com.skillverse.dto.HobbySkillDTO;
import com.skillverse.service.HobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/skills")
@CrossOrigin(origins = "*")
public class PublicSkillController {

    @Autowired
    private HobbyService hobbyService;

    @GetMapping
    public ResponseEntity<List<HobbySkillDTO>> getAllSkills() {
        return ResponseEntity.ok(hobbyService.getAllSkills());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HobbySkillDTO> getSkillById(@PathVariable Integer id) {
        return ResponseEntity.ok(hobbyService.getSkillById(id));
    }

    @GetMapping("/hobby/{hobbyId}")
    public ResponseEntity<List<HobbySkillDTO>> getSkillsByHobby(@PathVariable Integer hobbyId) {
        return ResponseEntity.ok(hobbyService.getSkillsByHobby(hobbyId));
    }
}