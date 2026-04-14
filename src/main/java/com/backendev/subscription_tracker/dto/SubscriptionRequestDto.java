package com.backendev.subscription_tracker.dto;

import com.backendev.subscription_tracker.entity.Category;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionRequestDto(

        @NotBlank(message = "Name is required!")
        String name,

        @NotNull(message = "Price is required!")
        @Positive(message = "Price must be positive")
        BigDecimal price,

        @NotNull(message = "Renewal Date is required!")
        @Future(message = "Renewal Date must be in future")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate renewalDate,

        Category category
) {
}