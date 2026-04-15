package com.backendev.subscription_tracker.dto;

import com.backendev.subscription_tracker.entity.Role;

public record UserResponseDto(
        Long id,
        String username,
        Role role
) {
}