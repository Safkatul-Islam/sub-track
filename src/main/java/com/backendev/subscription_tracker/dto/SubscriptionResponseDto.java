package com.backendev.subscription_tracker.dto;

import com.backendev.subscription_tracker.entity.Category;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SubscriptionResponseDto {
    private String name;
    private BigDecimal price;
    private LocalDate renewalDate;
    private Category category;
}
