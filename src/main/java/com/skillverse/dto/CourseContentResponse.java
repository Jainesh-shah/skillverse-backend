package com.skillverse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseContentResponse {
    private Integer contentId;
    private Integer courseId;
    private String contentTitle;
    private String videoUrl;
    private String duration;
    private LocalDateTime uploadedAt;
    
    public static CourseContentResponse fromEntity(com.skillverse.model.CourseContent content) {
        return new CourseContentResponse(
            content.getContentId(),
            content.getCourse().getCourseId(),
            content.getContentTitle(),
            content.getVideoUrl(),
            content.getDuration(),
            content.getUploadedAt()
        );
    }
}