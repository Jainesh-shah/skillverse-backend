package com.skillverse.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewItemDTO {
    private Integer reviewId;
    private String learnerName;
    private Integer rating;
    private String comment;
    private LocalDateTime reviewedAt;
}