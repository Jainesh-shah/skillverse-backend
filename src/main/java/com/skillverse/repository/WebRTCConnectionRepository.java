package com.skillverse.repository;

import com.skillverse.model.WebRTCConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebRTCConnectionRepository extends JpaRepository<WebRTCConnection, Integer> {
    
    List<WebRTCConnection> findBySession_SessionIdAndIsActive(
        Integer sessionId, Boolean isActive);
    
    Optional<WebRTCConnection> findBySessionSessionIdAndUserUserId(
        Integer sessionId, Integer userId);
    
    Optional<WebRTCConnection> findByPeerId(String peerId);
    
    @Query("SELECT wc FROM WebRTCConnection wc WHERE wc.session.sessionId = ?1 " +
           "AND wc.isActive = true")
    List<WebRTCConnection> findActiveConnectionsBySession(Integer sessionId);
    
    void deleteBySession_SessionId(Integer sessionId);
}
