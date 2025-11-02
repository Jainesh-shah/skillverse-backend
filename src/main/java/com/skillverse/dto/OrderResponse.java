package com.skillverse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String receipt;
    private String key; // Razorpay key for frontend
    private Integer courseId;
    private String courseName;
}