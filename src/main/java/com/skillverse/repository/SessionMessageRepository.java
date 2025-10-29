package com.skillverse.repository;

import com.skillverse.model.SessionMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionMessageRepository extends JpaRepository<SessionMessage, Integer> {
    
    List<SessionMessage> findBySession_SessionIdOrderBySentAtAsc(Integer sessionId);
    
    @Query("SELECT sm FROM SessionMessage sm WHERE sm.session.sessionId = ?1 " +
           "AND sm.sentAt >= ?2 ORDER BY sm.sentAt ASC")
    List<SessionMessage> findRecentMessages(Integer sessionId, LocalDateTime since);
    
    @Query("SELECT COUNT(sm) FROM SessionMessage sm WHERE sm.session.sessionId = ?1")
    Long countMessagesBySession(Integer sessionId);
}