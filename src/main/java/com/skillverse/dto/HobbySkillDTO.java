package com.skillverse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HobbySkillDTO {
    private Integer skillId;
    private String skillName;
    private String description;
    private Boolean isCustom;
    private Integer hobbyId;
    private String hobbyName;
}
