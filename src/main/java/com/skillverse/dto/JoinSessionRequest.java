package com.skillverse.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinSessionRequest {
    
    @NotNull(message = "Session ID is required")
    private Integer sessionId;
    
    private String deviceInfo;
    private Boolean videoEnabled = true;
    private Boolean audioEnabled = true;
}