package com.skillverse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaServerConfigDTO {
    private String roomId;
    private String mediaServerUrl;
    private List<IceServer> iceServers;
    private RouterCapabilities routerCapabilities;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IceServer {
        private List<String> urls;
        private String username;
        private String credential;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouterCapabilities {
        private Object codecs; // Will contain mediasoup RTP capabilities
        private Object headerExtensions;
    }
}