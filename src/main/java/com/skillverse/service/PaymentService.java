package com.skillverse.service;

import com.skillverse.dto.PaymentRequest;
import com.skillverse.model.Enrollment;
import com.skillverse.model.Payment;
import com.skillverse.repository.EnrollmentRepository;
import com.skillverse.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    @Transactional
    public Payment processPayment(PaymentRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollId())
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        
        // Mock payment processing
        Payment payment = new Payment();
        payment.setEnrollment(enrollment);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(Payment.PaymentMethod.valueOf(request.getPaymentMethod()));
        
        payment = paymentRepository.save(payment);
        
        // Update enrollment payment status
        enrollmentService.updatePaymentStatus(enrollment.getEnrollId(), Enrollment.PaymentStatus.Completed);
        
        return payment;
    }
    
    public List<Payment> getPaymentsByLearner(Integer learnerId) {
        return paymentRepository.findByEnrollment_Learner_UserId(learnerId);
    }
    
    public Payment getPaymentById(Integer paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }
}