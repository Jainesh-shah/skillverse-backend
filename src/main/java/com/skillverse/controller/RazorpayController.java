package com.skillverse.controller;

import com.razorpay.RazorpayException;
import com.skillverse.dto.*;
import com.skillverse.model.RazorpayOrder;
import com.skillverse.service.RazorpayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RazorpayController {
    
    private final RazorpayService razorpayService;
    
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        try {
            log.info("Received order creation request: {}", request);
            OrderResponse response = razorpayService.createOrder(request);
            return ResponseEntity.ok(response);
        } catch (RazorpayException e) {
            log.error("Razorpay exception while creating order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating order: " + e.getMessage());
        } catch (Exception e) {
            log.error("Exception while creating order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        try {
            log.info("Received payment verification request for order: {}", request.getRazorpayOrderId());
            PaymentVerificationResponse response = razorpayService.verifyPayment(request);
            
            if (response.isVerified()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("Exception while verifying payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new PaymentVerificationResponse(false, "Verification error: " + e.getMessage(), null, "ERROR"));
        }
    }
    
    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<?> getUserOrders(@PathVariable Integer userId) {
        try {
            List<RazorpayOrder> orders = razorpayService.getUserOrders(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error fetching user orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable String orderId) {
        try {
            RazorpayOrder order = razorpayService.getOrderById(orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error fetching order", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Order not found: " + e.getMessage());
        }
    }
}