package com.skillverse.repository;

import com.skillverse.model.SessionRecording;
import com.skillverse.model.SessionRecording.RecordingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRecordingRepository extends JpaRepository<SessionRecording, Integer> {

       List<SessionRecording> findBySession_SessionId(Integer sessionId);

       Optional<SessionRecording> findBySession_SessionIdAndStatus(
                     Integer sessionId,
                     SessionRecording.RecordingStatus status);

       boolean existsBySession_SessionIdAndStatus(
                     Integer sessionId,
                     SessionRecording.RecordingStatus status);

       List<SessionRecording> findByStatus(RecordingStatus status);

       @Query("SELECT sr FROM SessionRecording sr WHERE sr.session.course.courseId = ?1 " +
                     "AND sr.status = 'AVAILABLE' ORDER BY sr.startedAt DESC")
       List<SessionRecording> findAvailableRecordingsByCourse(Integer courseId);

       @Query("SELECT sr FROM SessionRecording sr WHERE sr.session.sessionId = ?1 " +
                     "AND sr.status = 'RECORDING'")
       List<SessionRecording> findActiveRecordingsBySession(Integer sessionId);
}
