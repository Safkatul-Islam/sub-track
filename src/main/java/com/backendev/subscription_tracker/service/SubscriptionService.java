package com.backendev.subscription_tracker.service;

import com.backendev.subscription_tracker.dto.SubscriptionRequestDto;
import com.backendev.subscription_tracker.dto.SubscriptionResponseDto;
import com.backendev.subscription_tracker.entity.Subscription;
import com.backendev.subscription_tracker.exception.SubscriptionAlreadyExistsException;
import com.backendev.subscription_tracker.exception.SubscriptionNotFoundException;
import com.backendev.subscription_tracker.mapper.SubscriptionMapper;
import com.backendev.subscription_tracker.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper mapper;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               SubscriptionMapper mapper) {
        this.subscriptionRepository = subscriptionRepository;
        this.mapper = mapper;
    }

    public SubscriptionResponseDto addSubscription(SubscriptionRequestDto requestDto) {
        if (subscriptionRepository.existsByName(requestDto.getName())) {
            throw new SubscriptionAlreadyExistsException("A subscription with this name already exists!");
        }

        Subscription subscription = subscriptionRepository.save(mapper.mapToEntity(requestDto));

        return mapper.mapToDto(subscription);
    }

    public List<SubscriptionResponseDto> getAllSubscriptions() {
        return subscriptionRepository.findAll()
                .stream()
                .map(mapper::mapToDto)
                .toList();
    }

    public SubscriptionResponseDto updateSubscription(Long id, SubscriptionRequestDto requestDto) {
        Subscription findSubscription = subscriptionRepository.findById(id).orElseThrow(() -> new SubscriptionNotFoundException("No subscription with ID " + id + " exists!"));

        findSubscription.setName(requestDto.getName());
        findSubscription.setPrice(requestDto.getPrice());
        findSubscription.setRenewalDate(requestDto.getRenewalDate());
        findSubscription.setCategory(requestDto.getCategory());

        return mapper.mapToDto(subscriptionRepository.save(findSubscription));
    }

    public void deleteSubscription(Long id) {
        Subscription findSubscription = subscriptionRepository.findById(id).orElseThrow(() -> new SubscriptionNotFoundException("No subscription with ID " + id + " exists!"));

        subscriptionRepository.delete(findSubscription);
    }

    public BigDecimal totalMonthlyCost() {
        return subscriptionRepository.findAll()
                .stream()
                .map(Subscription::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<SubscriptionResponseDto> getUpcomingRenewals() {
        LocalDate time = LocalDate.now();
        LocalDate weekFromNow = time.plusDays(7);

        return subscriptionRepository.findAll()
                .stream()
                .filter(subscription -> !subscription.getRenewalDate().isBefore(time) && !subscription.getRenewalDate().isAfter(weekFromNow))
                .map(mapper::mapToDto)
                .toList();
    }
}
