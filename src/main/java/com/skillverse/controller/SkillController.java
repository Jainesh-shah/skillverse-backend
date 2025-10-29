package com.skillverse.controller;

import com.skillverse.dto.HobbySkillDTO;
import com.skillverse.dto.MessageResponse;
import com.skillverse.model.HobbySkill;
import com.skillverse.service.HobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/skills")
@CrossOrigin(origins = "*")
public class SkillController {

    @Autowired
    private HobbyService hobbyService;

    @GetMapping("/hobby/{hobbyId}")
    public ResponseEntity<List<HobbySkillDTO>> getSkillsByHobby(@PathVariable Integer hobbyId) {
        return ResponseEntity.ok(hobbyService.getSkillsByHobby(hobbyId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HobbySkillDTO> getSkillById(@PathVariable Integer id) {
        return ResponseEntity.ok(hobbyService.getSkillById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<?> createSkill(@RequestBody HobbySkill skill) {
        try {
            HobbySkillDTO created = hobbyService.createSkill(skill);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
