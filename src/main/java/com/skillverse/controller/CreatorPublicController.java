package com.skillverse.controller;

import com.skillverse.dto.CreatorDetailDTO;
import com.skillverse.dto.CreatorPublicDTO;
import com.skillverse.service.CreatorPublicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/public/creators")
@CrossOrigin(origins = "*")
public class CreatorPublicController {
    
    @Autowired
    private CreatorPublicService creatorPublicService;
    
    @GetMapping
    public ResponseEntity<List<CreatorPublicDTO>> getAllCreators() {
        return ResponseEntity.ok(creatorPublicService.getAllCreators());
    }
    
    @GetMapping("/{creatorId}")
    public ResponseEntity<CreatorDetailDTO> getCreatorDetail(@PathVariable Integer creatorId) {
        return ResponseEntity.ok(creatorPublicService.getCreatorDetail(creatorId));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<CreatorPublicDTO>> searchCreators(@RequestParam String keyword) {
        return ResponseEntity.ok(creatorPublicService.searchCreators(keyword));
    }
    
    @GetMapping("/skill/{skillId}")
    public ResponseEntity<List<CreatorPublicDTO>> getCreatorsBySkill(@PathVariable Integer skillId) {
        return ResponseEntity.ok(creatorPublicService.getCreatorsBySkill(skillId));
    }

    @GetMapping("/skills")
    public ResponseEntity<List<CreatorPublicDTO>> getCreatorsBySkills(@RequestParam String skillIds) {
        List<Integer> skillIdList = Arrays.stream(skillIds.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        return ResponseEntity.ok(creatorPublicService.getCreatorsBySkills(skillIdList));
    }

}