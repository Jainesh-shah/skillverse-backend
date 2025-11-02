package com.skillverse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

// DTO for creating order request
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private Integer courseId;
    private Integer userId;
    private BigDecimal amount;
}