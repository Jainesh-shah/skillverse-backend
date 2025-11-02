package com.skillverse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enroll_id")
    private Integer enrollId;
    
    @Column(name = "enrolled_at")
    private LocalDateTime enrolledAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id", nullable = false)
    private User learner;
    
    // Add this relationship
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "razorpay_order_id")
    private RazorpayOrder razorpayOrder;
    
    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL)
    private java.util.List<Payment> payments;
    
    public enum PaymentStatus {
        Pending,
        Completed
    }
}