package com.skillverse.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;  // ← Add this import

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    
    @Value("${recording.storage.path}")
    private String uploadDir;
    
    private static final String VIDEO_DIRECTORY = "course-videos";

     @PostConstruct
    public void initializeStorage() {
        try {
            Path videoPath = Paths.get(uploadDir, VIDEO_DIRECTORY);
            Files.createDirectories(videoPath);
            System.out.println("✅ Storage initialized at: " + videoPath.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directories", e);
        }
    }
    
    /**
     * Initialize storage directories
     */
    public void init() {
        try {
            Path videoPath = Paths.get(uploadDir, VIDEO_DIRECTORY);
            if (!Files.exists(videoPath)) {
                Files.createDirectories(videoPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directories", e);
        }
    }
    
    /**
     * Save video file to local storage
     * @param file MultipartFile to save
     * @return Relative URL path to access the file
     */
    public String saveVideo(MultipartFile file) {
    if (file.isEmpty()) {
        throw new RuntimeException("Failed to store empty file");
    }
    
    try {
        // Initialize directories if not exist
        init();
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Validate file type
        validateVideoFile(fileExtension);
        
        // Create the full path
        Path videoDirectory = Paths.get(uploadDir, VIDEO_DIRECTORY).toAbsolutePath().normalize();
        Path destinationFile = videoDirectory.resolve(uniqueFilename).normalize();
        
        // Security check - ensure the file path is within the upload directory
        if (!destinationFile.startsWith(videoDirectory)) {
            throw new RuntimeException("Cannot store file outside designated directory");
        }
        
        // Copy file to destination
        Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative URL path
        return "/" + VIDEO_DIRECTORY + "/" + uniqueFilename;
        
    } catch (IOException e) {
        throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
    }
}
    
    /**
     * Delete video file from storage
     * @param videoUrl The URL/path of the video to delete
     * @return true if deleted successfully
     */
    public boolean deleteVideo(String videoUrl) {
        try {
            if (videoUrl == null || videoUrl.isEmpty()) {
                return false;
            }
            
            // Extract filename from URL (remove leading slash if present)
            String filename = videoUrl.startsWith("/") ? videoUrl.substring(1) : videoUrl;
            Path filePath = Paths.get(uploadDir, filename);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get the absolute path of a video file
     * @param videoUrl The relative URL of the video
     * @return Absolute file path
     */
    public Path getVideoPath(String videoUrl) {
        if (videoUrl == null || videoUrl.isEmpty()) {
            throw new RuntimeException("Video URL is empty");
        }
        
        String filename = videoUrl.startsWith("/") ? videoUrl.substring(1) : videoUrl;
        return Paths.get(uploadDir, filename);
    }
    
    /**
     * Check if video file exists
     * @param videoUrl The URL of the video
     * @return true if exists
     */
    public boolean videoExists(String videoUrl) {
        try {
            Path videoPath = getVideoPath(videoUrl);
            return Files.exists(videoPath);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get file size in bytes
     * @param videoUrl The URL of the video
     * @return File size in bytes
     */
    public long getFileSize(String videoUrl) {
        try {
            Path videoPath = getVideoPath(videoUrl);
            if (Files.exists(videoPath)) {
                return Files.size(videoPath);
            }
            return 0;
        } catch (IOException e) {
            throw new RuntimeException("Failed to get file size: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            throw new RuntimeException("Invalid filename");
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            throw new RuntimeException("File has no extension");
        }
        
        return filename.substring(lastDotIndex);
    }
    
    /**
     * Validate if the file is a valid video format
     */
    private void validateVideoFile(String fileExtension) {
        String extension = fileExtension.toLowerCase();
        if (!extension.equals(".mp4") && 
            !extension.equals(".webm") && 
            !extension.equals(".ogg") && 
            !extension.equals(".mov") &&
            !extension.equals(".avi") &&
            !extension.equals(".mkv")) {
            throw new RuntimeException("Invalid video format. Supported formats: MP4, WebM, OGG, MOV, AVI, MKV");
        }
    }
    
    /**
     * Get content type based on file extension
     */
    public String getContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        switch (extension) {
            case ".mp4":
                return "video/mp4";
            case ".webm":
                return "video/webm";
            case ".ogg":
                return "video/ogg";
            case ".mov":
                return "video/quicktime";
            case ".avi":
                return "video/x-msvideo";
            case ".mkv":
                return "video/x-matroska";
            default:
                return "application/octet-stream";
        }
    }
}