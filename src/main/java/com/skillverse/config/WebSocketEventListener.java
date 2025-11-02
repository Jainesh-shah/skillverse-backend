package com.skillverse.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
public class WebSocketEventListener {

        private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);


    @EventListener
public void handleWebSocketConnectListener(SessionConnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = headerAccessor.getSessionId();
    String user = headerAccessor.getNativeHeader("Authorization") != null
        ? headerAccessor.getNativeHeader("Authorization").get(0)
        : "Anonymous";

    log.info("WebSocket CONNECT event - Session: {}, Auth: {}", sessionId, user);
}


    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        System.out.println("WebSocket Disconnected - Session ID: " + sessionId);
    }

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(
            event.getMessage(), StompHeaderAccessor.class);
        
        String destination = headerAccessor.getDestination();
        String sessionId = headerAccessor.getSessionId();
        
        System.out.println("WebSocket Subscription - Session: " + sessionId + ", Destination: " + destination);
    }
}