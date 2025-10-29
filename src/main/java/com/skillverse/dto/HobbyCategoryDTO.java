package com.skillverse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HobbyCategoryDTO {
    private Integer hobbyId;
    private String hobbyName;
    private String description;
}
