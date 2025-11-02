package com.skillverse.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.skillverse.dto.*;
import com.skillverse.model.*;
import com.skillverse.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RazorpayService {
    
    private final RazorpayOrderRepository razorpayOrderRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentRepository paymentRepository;
    
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;
    
    @Transactional
    public OrderResponse createOrder(OrderRequest request) throws RazorpayException {
        log.info("Creating Razorpay order for user: {} and course: {}", request.getUserId(), request.getCourseId());
        
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> new RuntimeException("Course not found"));
        
        RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", request.getAmount().multiply(new BigDecimal("100")).intValue());
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "rcpt_" + System.currentTimeMillis());
        // Remove payment_capture - it's not needed for order creation
        
        Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        log.info("Created Razorpay order details: {}", razorpayOrder.toString());

        
        RazorpayOrder order = new RazorpayOrder();
        order.setOrderId(razorpayOrder.get("id"));
        order.setAmount(request.getAmount());
        order.setCurrency("INR");
        order.setReceipt(razorpayOrder.get("receipt"));
        order.setStatus(RazorpayOrder.OrderStatus.CREATED);
        order.setUser(user);
        order.setCourse(course);
        
        razorpayOrderRepository.save(order);
        
        log.info("Razorpay order created successfully: {}", order.getOrderId());
        
        return new OrderResponse(
            order.getOrderId(),
            order.getAmount(),
            order.getCurrency(),
            order.getReceipt(),
            razorpayKeyId,
            course.getCourseId(),
            course.getTitle()
        );
    }
    
    @Transactional
    public PaymentVerificationResponse verifyPayment(PaymentVerificationRequest request) {
        log.info("Verifying payment for order: {}", request.getRazorpayOrderId());
        
        try {
            String generatedSignature = generateSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId()
            );
            
            if (!generatedSignature.equals(request.getRazorpaySignature())) {
                log.error("Payment signature verification failed");
                return new PaymentVerificationResponse(false, "Invalid signature", null, "FAILED");
            }
            
            RazorpayOrder order = razorpayOrderRepository.findByOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
            
            order.setRazorpayPaymentId(request.getRazorpayPaymentId());
            order.setRazorpaySignature(request.getRazorpaySignature());
            order.setStatus(RazorpayOrder.OrderStatus.CAPTURED);
            razorpayOrderRepository.save(order);
            
            Enrollment enrollment = new Enrollment();
            enrollment.setCourse(order.getCourse());
            enrollment.setLearner(order.getUser());
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setPaymentStatus(Enrollment.PaymentStatus.Completed);
            enrollment = enrollmentRepository.save(enrollment);
            
            order.setEnrollment(enrollment);
            razorpayOrderRepository.save(order);
            
            Payment payment = new Payment();
            payment.setEnrollment(enrollment);
            payment.setAmount(order.getAmount());
            payment.setPaymentMethod(Payment.PaymentMethod.UPI);
            payment.setPaymentDate(LocalDateTime.now());
            paymentRepository.save(payment);
            
            log.info("Payment verified and enrollment created successfully");
            
            return new PaymentVerificationResponse(
                true,
                "Payment verified successfully",
                enrollment.getEnrollId(),
                "SUCCESS"
            );
            
        } catch (Exception e) {
            log.error("Error verifying payment", e);
            return new PaymentVerificationResponse(false, "Verification failed: " + e.getMessage(), null, "FAILED");
        }
    }
    
    private String generateSignature(String orderId, String paymentId) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(razorpayKeySecret.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Error generating signature", e);
            throw new RuntimeException("Error generating signature", e);
        }
    }
    
    public List<RazorpayOrder> getUserOrders(Integer userId) {
        return razorpayOrderRepository.findByUserUserId(userId);
    }
    
    public RazorpayOrder getOrderById(String orderId) {
        return razorpayOrderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}