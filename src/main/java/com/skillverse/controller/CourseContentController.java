package com.skillverse.controller;

import com.skillverse.dto.CourseContentResponse;
import com.skillverse.dto.MessageResponse;
import com.skillverse.model.CourseContent;
import com.skillverse.service.CourseContentService;
import com.skillverse.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/course-content")
public class CourseContentController {

    @Autowired
    private CourseContentService courseContentService;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Add new course content with video upload
     */
    @PostMapping("/add/{courseId}")
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<?> addContent(
            @PathVariable Integer courseId,
            @RequestParam("contentTitle") String contentTitle,
            @RequestParam("videoFile") MultipartFile videoFile,
            @RequestParam(value = "duration", required = false) String duration,
            Authentication authentication) {
        try {
            // Validate file
            if (videoFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Please select a video file to upload"));
            }

            // Save video file
            String videoUrl = fileStorageService.saveVideo(videoFile);

            // Create course content
            CourseContent content = new CourseContent();
            content.setContentTitle(contentTitle);
            content.setVideoUrl(videoUrl);
            content.setDuration(duration);

            // Save to database
            CourseContent savedContent = courseContentService.addContent(content, courseId);

            // Return DTO instead of entity
            CourseContentResponse response = CourseContentResponse.fromEntity(savedContent);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to upload content: " + e.getMessage()));
        }
    }

    /**
     * Get content by ID
     */
    @GetMapping("/{contentId}")
    public ResponseEntity<?> getContent(@PathVariable Integer contentId) {
        try {
            CourseContent content = courseContentService.getContentById(contentId);
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Update course content
     */
    @PutMapping("/update/{contentId}")
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<?> updateContent(
            @PathVariable Integer contentId,
            @RequestParam(value = "contentTitle", required = false) String contentTitle,
            @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
            @RequestParam(value = "duration", required = false) String duration) {
        try {
            CourseContent updatedContent = new CourseContent();
            updatedContent.setContentTitle(contentTitle);
            updatedContent.setDuration(duration);

            // If new video file is uploaded
            if (videoFile != null && !videoFile.isEmpty()) {
                // Get old content to delete old video
                CourseContent oldContent = courseContentService.getContentById(contentId);
                String oldVideoUrl = oldContent.getVideoUrl();

                // Upload new video
                String newVideoUrl = fileStorageService.saveVideo(videoFile);
                updatedContent.setVideoUrl(newVideoUrl);

                // Delete old video file
                if (oldVideoUrl != null && !oldVideoUrl.isEmpty()) {
                    fileStorageService.deleteVideo(oldVideoUrl);
                }
            }

            CourseContent result = courseContentService.updateContent(contentId, updatedContent);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to update content: " + e.getMessage()));
        }
    }

    /**
     * Delete course content
     */
    @DeleteMapping("/delete/{contentId}")
    @PreAuthorize("hasRole('Creator')")
    public ResponseEntity<?> deleteContent(@PathVariable Integer contentId) {
        try {
            // Get content to delete associated video file
            CourseContent content = courseContentService.getContentById(contentId);
            String videoUrl = content.getVideoUrl();

            // Delete from database
            courseContentService.deleteContent(contentId);

            // Delete video file
            if (videoUrl != null && !videoUrl.isEmpty()) {
                fileStorageService.deleteVideo(videoUrl);
            }

            return ResponseEntity.ok(new MessageResponse("Content deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to delete content: " + e.getMessage()));
        }
    }

    /**
     * Stream/Download video file
     */
    @GetMapping("/video/**")
    public ResponseEntity<Resource> getVideo(@RequestParam String url) {
        try {
            Path videoPath = fileStorageService.getVideoPath(url);
            Resource resource = new UrlResource(videoPath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = fileStorageService.getContentType(url);

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all content for a course
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getCourseContent(@PathVariable Integer courseId) {
        try {
            return ResponseEntity.ok(courseContentService.getContentByCourse(courseId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }
}