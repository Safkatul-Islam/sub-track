package com.backendev.subscription_tracker.controller;

import com.backendev.subscription_tracker.dto.SubscriptionRequestDto;
import com.backendev.subscription_tracker.dto.SubscriptionResponseDto;
import com.backendev.subscription_tracker.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    public ResponseEntity<Page<SubscriptionResponseDto>> getAllSubscriptions(Pageable pageable) {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions(pageable));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<SubscriptionResponseDto>> getUpcomingRenewals() {
        return ResponseEntity.ok(subscriptionService.getUpcomingRenewals());
    }

    @GetMapping("/total-cost")
    public ResponseEntity<Map<String, BigDecimal>> getTotalMonthlyCost() {
        return ResponseEntity.ok(Map.of("totalMonthlyCost", subscriptionService.totalMonthlyCost()));
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponseDto> addSubscription(@Valid @RequestBody SubscriptionRequestDto requestDto) {
        SubscriptionResponseDto created = subscriptionService.addSubscription(requestDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponseDto> updateSubscription(@PathVariable Long id,
                                                                      @Valid @RequestBody SubscriptionRequestDto requestDto) {
        return ResponseEntity.ok(subscriptionService.updateSubscription(id, requestDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long id) {
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }
}