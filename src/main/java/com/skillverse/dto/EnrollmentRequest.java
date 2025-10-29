package com.skillverse.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnrollmentRequest {
    @NotNull
    private Integer courseId;
}