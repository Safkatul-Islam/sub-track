package com.backendev.subscription_tracker.mapper;

import com.backendev.subscription_tracker.dto.SubscriptionRequestDto;
import com.backendev.subscription_tracker.dto.SubscriptionResponseDto;
import com.backendev.subscription_tracker.entity.Subscription;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

    public Subscription mapToEntity(SubscriptionRequestDto requestDto) {
        Subscription subscription = new Subscription();
        subscription.setName(requestDto.name());
        subscription.setPrice(requestDto.price());
        subscription.setRenewalDate(requestDto.renewalDate());
        subscription.setCategory(requestDto.category());
        return subscription;
    }

    public SubscriptionResponseDto mapToDto(Subscription subscription) {
        return new SubscriptionResponseDto(
                subscription.getId(),
                subscription.getName(),
                subscription.getPrice(),
                subscription.getRenewalDate(),
                subscription.getCategory()
        );
    }
}