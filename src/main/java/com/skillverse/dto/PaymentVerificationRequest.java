package com.skillverse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerificationRequest {
    private String orderId;
    private String razorpayPaymentId;
    private String razorpayOrderId;
    private String razorpaySignature;
}