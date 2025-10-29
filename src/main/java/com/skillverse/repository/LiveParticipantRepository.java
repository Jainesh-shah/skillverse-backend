package com.skillverse.repository;

import com.skillverse.model.LiveParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiveParticipantRepository extends JpaRepository<LiveParticipant, Integer> {

    List<LiveParticipant> findBySession_SessionId(Integer sessionId);
    
    List<LiveParticipant> findBySession_SessionIdAndIsCurrentlyConnected(
            Integer sessionId, 
            Boolean isCurrentlyConnected
    );
    
    Optional<LiveParticipant> findBySession_SessionIdAndLearner_UserId(
            Integer sessionId, 
            Integer userId
    );
    
    int countBySession_SessionIdAndIsCurrentlyConnected(
            Integer sessionId, 
            Boolean isCurrentlyConnected
    );
    
    @Query("SELECT lp FROM LiveParticipant lp WHERE lp.session.sessionId = ?1 " +
           "AND lp.isCurrentlyConnected = true")
    List<LiveParticipant> findActiveParticipants(Integer sessionId);
    
    @Query("SELECT COUNT(lp) FROM LiveParticipant lp WHERE lp.session.sessionId = ?1 " +
           "AND lp.isCurrentlyConnected = true")
    Integer countActiveParticipants(Integer sessionId);
    
    boolean existsBySession_SessionIdAndLearner_UserId(Integer sessionId, Integer learnerId);
}
