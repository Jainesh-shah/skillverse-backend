package com.skillverse.repository;

import com.skillverse.model.SessionAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SessionAnalyticsRepository extends JpaRepository<SessionAnalytics, Integer> {
    
    Optional<SessionAnalytics> findBySession_SessionId(Integer sessionId);
    
    boolean existsBySession_SessionId(Integer sessionId);
}