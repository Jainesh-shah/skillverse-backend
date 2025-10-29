package com.skillverse.repository;

import com.skillverse.model.LiveSession;
import com.skillverse.model.LiveSession.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiveSessionRepository extends JpaRepository<LiveSession, Integer> {

        List<LiveSession> findByCourse_CourseId(Integer courseId);
    
    List<LiveSession> findByCreator_CreatorIdAndStatusOrderByStartTimeAsc(
            Integer creatorId, 
            LiveSession.SessionStatus status
    );
    
    List<LiveSession> findByStatusOrderByActualStartTimeDesc(LiveSession.SessionStatus status);
    
    List<LiveSession> findByCreator_CreatorId(Integer creatorId);
    
    Optional<LiveSession> findByRoomId(String roomId);
    
    List<LiveSession> findByStatus(SessionStatus status);
    
    @Query("SELECT ls FROM LiveSession ls WHERE ls.status = 'SCHEDULED' " +
           "AND ls.startTime BETWEEN ?1 AND ?2")
    List<LiveSession> findUpcomingSessions(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT ls FROM LiveSession ls WHERE ls.creator.creatorId = ?1 " +
           "AND ls.status = ?2 ORDER BY ls.startTime DESC")
    List<LiveSession> findByCreatorAndStatus(Integer creatorId, SessionStatus status);
    
    @Query("SELECT ls FROM LiveSession ls WHERE ls.recordingEnabled = true " +
           "AND ls.status = 'LIVE'")
    List<LiveSession> findActiveRecordingSessions();
    
    List<LiveSession> findByStartTimeAfter(LocalDateTime time);

    List<LiveSession> findByEndTimeBefore(LocalDateTime time);
}
