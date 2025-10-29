package com.skillverse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Integer userId;
    private String email;
    private String userType;
    
    public JwtResponse(String token, Integer userId, String email, String userType) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.userType = userType;
    }
}