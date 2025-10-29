package com.skillverse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enroll_id", nullable = false)
    private Enrollment enrollment;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;
    
    @CreationTimestamp
    @Column(name = "payment_date", updatable = false)
    private LocalDateTime paymentDate;
    
    public enum PaymentMethod {
        CreditCard("Credit Card"),
        UPI("UPI"),
        PayPal("PayPal");
        
        private final String displayName;
        
        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}