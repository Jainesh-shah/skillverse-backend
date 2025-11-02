package com.skillverse.repository;

import com.skillverse.model.RazorpayOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RazorpayOrderRepository extends JpaRepository<RazorpayOrder, Integer> {
    
    Optional<RazorpayOrder> findByOrderId(String orderId);
    
    Optional<RazorpayOrder> findByRazorpayPaymentId(String razorpayPaymentId);
    
    List<RazorpayOrder> findByUserUserId(Integer userId);
    
    List<RazorpayOrder> findByUserUserIdAndStatus(Integer userId, RazorpayOrder.OrderStatus status);
    
    List<RazorpayOrder> findByCourseCourseIdAndStatus(Integer courseId, RazorpayOrder.OrderStatus status);
    
    boolean existsByOrderId(String orderId);
}