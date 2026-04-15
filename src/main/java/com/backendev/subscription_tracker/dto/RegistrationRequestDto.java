package com.backendev.subscription_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequestDto(

        @NotBlank(message = "Username is required!")
        @Size(min = 3, max = 64, message = "Username must be between 3 and 64 characters")
        String username,

        @NotBlank(message = "Password is required!")
        @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
        String password
) {
}