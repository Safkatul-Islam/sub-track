package com.backendev.subscription_tracker.dto;

import com.backendev.subscription_tracker.entity.Category;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionResponseDto(
        Long id,
        String name,
        BigDecimal price,
        LocalDate renewalDate,
        Category category
) {
}