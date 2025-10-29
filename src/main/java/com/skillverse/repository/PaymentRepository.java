package com.skillverse.repository;

import com.skillverse.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByEnrollment_Learner_UserId(Integer learnerId);
}