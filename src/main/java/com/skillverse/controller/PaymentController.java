package com.skillverse.controller;

import com.skillverse.dto.MessageResponse;
import com.skillverse.dto.PaymentRequest;
import com.skillverse.model.Payment;
import com.skillverse.security.UserDetailsServiceImpl;
import com.skillverse.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @PostMapping
    @PreAuthorize("hasRole('Learner')")
    public ResponseEntity<?> processPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            Payment payment = paymentService.processPayment(request);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/my-payments")
    @PreAuthorize("hasRole('Learner')")
    public ResponseEntity<List<Payment>> getMyPayments(Authentication authentication) {
        String email = authentication.getName();
        Integer userId = userDetailsService.loadUserEntityByEmail(email).getUserId();
        return ResponseEntity.ok(paymentService.getPaymentsByLearner(userId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Integer id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }
}