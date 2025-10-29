package com.skillverse.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ContentItemDTO {
    private Integer contentId;
    private String contentTitle;
    private String videoUrl;
    private String duration;
    private LocalDateTime uploadedAt;
    private Boolean isCompleted;
    private Integer orderIndex;
}
