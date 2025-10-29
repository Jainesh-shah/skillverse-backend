package com.skillverse.controller;

import com.skillverse.dto.HobbyCategoryDTO;
import com.skillverse.dto.MessageResponse;
import com.skillverse.model.HobbyCategory;
import com.skillverse.service.HobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hobbies")
@CrossOrigin(origins = "*")
public class HobbyController {

    @Autowired
    private HobbyService hobbyService;

    @GetMapping
    public ResponseEntity<List<HobbyCategoryDTO>> getAllHobbies() {
        return ResponseEntity.ok(hobbyService.getAllHobbies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HobbyCategoryDTO> getHobbyById(@PathVariable Integer id) {
        return ResponseEntity.ok(hobbyService.getHobbyById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<?> createHobby(@RequestBody HobbyCategory hobby) {
        try {
            HobbyCategoryDTO created = hobbyService.createHobby(hobby);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
