package com.skillverse.repository;

import com.skillverse.model.RecordingAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecordingAccessRepository extends JpaRepository<RecordingAccess, Integer> {

    Optional<RecordingAccess> findByRecording_RecordingIdAndLearner_UserId(
            Integer recordingId, 
            Integer userId
    );
    
    boolean existsByRecording_RecordingIdAndLearner_UserId(
            Integer recordingId, 
            Integer userId
    );
        
    List<RecordingAccess> findByLearner_UserId(Integer learnerId);
    
    List<RecordingAccess> findByRecording_RecordingId(Integer recordingId);
    
    
    @Query("SELECT COUNT(ra) FROM RecordingAccess ra WHERE ra.recording.recordingId = ?1")
    Long countAccessByRecording(Integer recordingId);
}