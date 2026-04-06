package com.backendev.subscription_tracker.dto;

import com.backendev.subscription_tracker.entity.Category;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SubscriptionRequestDto {

    @NotBlank(message = "Name is required!")
    private String name;

    @NotNull(message = "Price is required!")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Renewal Date is required!")
    @Future(message = "Renewal Date must be in future")
    @JsonFormat(pattern = "yyyy-mm-dd")
    private LocalDate renewalDate;

    private Category category;
}
