package com.skillverse.controller;

import com.skillverse.dto.MessageResponse;
import com.skillverse.model.Wishlist;
import com.skillverse.security.UserDetailsServiceImpl;
import com.skillverse.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('Learner')")
public class WishlistController {
    
    @Autowired
    private WishlistService wishlistService;
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @PostMapping("/add/{courseId}")
    public ResponseEntity<?> addToWishlist(@PathVariable Integer courseId,
                                           Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            Wishlist wishlist = wishlistService.addToWishlist(courseId, userId);
            return ResponseEntity.ok(wishlist);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Wishlist>> getWishlist(Authentication authentication) {
        String email = authentication.getName();
        Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
        return ResponseEntity.ok(wishlistService.getWishlist(userId));
    }
    
    @DeleteMapping("/remove/{courseId}")
    public ResponseEntity<?> removeFromWishlist(@PathVariable Integer courseId,
                                                Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
            wishlistService.removeFromWishlist(courseId, userId);
            return ResponseEntity.ok(new MessageResponse("Removed from wishlist"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}