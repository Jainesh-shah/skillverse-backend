package com.skillverse.service;

import com.skillverse.dto.CreatorProfileRequest;
import com.skillverse.dto.CreatorProfileResponse;
import com.skillverse.model.CreatorProfile;
import com.skillverse.model.User;
import com.skillverse.repository.CreatorProfileRepository;
import com.skillverse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreatorProfileService {
    
    @Autowired
    private CreatorProfileRepository creatorProfileRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public CreatorProfileResponse getCreatorProfile(Integer userId) {
        CreatorProfile profile = creatorProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Creator profile not found"));
        
        return convertToResponse(profile);
    }
    
    @Transactional
    public CreatorProfileResponse createOrUpdateProfile(Integer userId, CreatorProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if profile exists
        CreatorProfile profile = creatorProfileRepository.findByUser_UserId(userId)
                .orElse(new CreatorProfile());
        
        // If new profile, set user
        if (profile.getCreatorId() == null) {
            profile.setUser(user);
            profile.setVerified(false);
        }
        
        // Update fields
        if (request.getExperienceYears() != null) {
            profile.setExperienceYears(request.getExperienceYears());
        }
        if (request.getQualification() != null) {
            profile.setQualification(request.getQualification());
        }
        if (request.getLocation() != null) {
            profile.setLocation(request.getLocation());
        }
        if (request.getSocialLinks() != null) {
            profile.setSocialLinks(request.getSocialLinks());
        }
        
        profile = creatorProfileRepository.save(profile);
        
        return convertToResponse(profile);
    }
    
    public boolean hasCreatorProfile(Integer userId) {
        return creatorProfileRepository.findByUser_UserId(userId).isPresent();
    }
    
    private CreatorProfileResponse convertToResponse(CreatorProfile profile) {
        CreatorProfileResponse response = new CreatorProfileResponse();
        response.setCreatorId(profile.getCreatorId());
        response.setUserId(profile.getUser().getUserId());
        response.setFirstName(profile.getUser().getFirstName());
        response.setLastName(profile.getUser().getLastName());
        response.setEmail(profile.getUser().getEmail());
        response.setExperienceYears(profile.getExperienceYears());
        response.setQualification(profile.getQualification());
        response.setLocation(profile.getLocation());
        response.setVerified(profile.getVerified());
        response.setSocialLinks(profile.getSocialLinks());
        return response;
    }
}