package com.backendev.subscription_tracker.service;

import com.backendev.subscription_tracker.dto.SubscriptionRequestDto;
import com.backendev.subscription_tracker.dto.SubscriptionResponseDto;
import com.backendev.subscription_tracker.entity.Subscription;
import com.backendev.subscription_tracker.exception.SubscriptionAlreadyExistsException;
import com.backendev.subscription_tracker.exception.SubscriptionNotFoundException;
import com.backendev.subscription_tracker.mapper.SubscriptionMapper;
import com.backendev.subscription_tracker.repository.SubscriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper mapper;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               SubscriptionMapper mapper) {
        this.subscriptionRepository = subscriptionRepository;
        this.mapper = mapper;
    }

    @Transactional
    public SubscriptionResponseDto addSubscription(SubscriptionRequestDto requestDto) {
        if (subscriptionRepository.existsByName(requestDto.name())) {
            throw new SubscriptionAlreadyExistsException(
                    "A subscription with the name '" + requestDto.name() + "' already exists!");
        }

        Subscription saved = subscriptionRepository.save(mapper.mapToEntity(requestDto));
        return mapper.mapToDto(saved);
    }

    public Page<SubscriptionResponseDto> getAllSubscriptions(Pageable pageable) {
        return subscriptionRepository.findAll(pageable).map(mapper::mapToDto);
    }

    @Transactional
    public SubscriptionResponseDto updateSubscription(Long id, SubscriptionRequestDto requestDto) {
        Subscription found = subscriptionRepository.findById(id)
                .orElseThrow(() -> new SubscriptionNotFoundException("No subscription with ID " + id + " exists!"));

        found.setName(requestDto.name());
        found.setPrice(requestDto.price());
        found.setRenewalDate(requestDto.renewalDate());
        found.setCategory(requestDto.category());

        return mapper.mapToDto(found);
    }

    @Transactional
    public void deleteSubscription(Long id) {
        Subscription found = subscriptionRepository.findById(id)
                .orElseThrow(() -> new SubscriptionNotFoundException("No subscription with ID " + id + " exists!"));

        subscriptionRepository.delete(found);
    }

    public BigDecimal totalMonthlyCost() {
        return subscriptionRepository.findAll()
                .stream()
                .map(Subscription::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<SubscriptionResponseDto> getUpcomingRenewals() {
        LocalDate today = LocalDate.now();
        LocalDate weekFromNow = today.plusDays(7);

        return subscriptionRepository.findByRenewalDateBetween(today, weekFromNow)
                .stream()
                .map(mapper::mapToDto)
                .toList();
    }
}