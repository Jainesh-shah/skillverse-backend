package com.skillverse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${recording.storage.path}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve course videos
        String videoPath = Paths.get(uploadDir).toAbsolutePath().toUri().toString();
        
        registry.addResourceHandler("/course-videos/**")
                .addResourceLocations(videoPath + "course-videos/")
                .setCachePeriod(3600); // Cache for 1 hour
        
        // Serve recordings (if you have other recording features)
        registry.addResourceHandler("/recordings/**")
                .addResourceLocations(videoPath)
                .setCachePeriod(3600);
    }
}