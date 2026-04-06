package com.backendev.subscription_tracker.mapper;

import com.backendev.subscription_tracker.dto.SubscriptionRequestDto;
import com.backendev.subscription_tracker.dto.SubscriptionResponseDto;
import com.backendev.subscription_tracker.entity.Subscription;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

    public Subscription mapToEntity(SubscriptionRequestDto requestDto) {
        Subscription subscription = new Subscription();

        subscription.setName(requestDto.getName());
        subscription.setPrice(requestDto.getPrice());
        subscription.setRenewalDate(requestDto.getRenewalDate());
        subscription.setCategory(requestDto.getCategory());

        return subscription;
    }

    public SubscriptionResponseDto mapToDto(Subscription subscription) {
        SubscriptionResponseDto responseDto = new SubscriptionResponseDto();

        responseDto.setName(subscription.getName());
        responseDto.setPrice(subscription.getPrice());
        responseDto.setRenewalDate(subscription.getRenewalDate());
        responseDto.setCategory(subscription.getCategory());

        return responseDto;
    }
}
