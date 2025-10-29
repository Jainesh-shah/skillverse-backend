package com.skillverse.repository;

import com.skillverse.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Integer> {
    @Query("SELECT c FROM Chat c WHERE (c.sender.userId = :userId1 AND c.receiver.userId = :userId2) OR (c.sender.userId = :userId2 AND c.receiver.userId = :userId1) ORDER BY c.sentAt ASC")
    List<Chat> findChatBetweenUsers(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);
}