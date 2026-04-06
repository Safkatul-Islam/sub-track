package com.backendev.subscription_tracker.controller;

import com.backendev.subscription_tracker.dto.SubscriptionRequestDto;
import com.backendev.subscription_tracker.dto.SubscriptionResponseDto;
import com.backendev.subscription_tracker.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionResponseDto>> getAllSubscriptions() {
        List<SubscriptionResponseDto> subscriptions = subscriptionService.getAllSubscriptions();
        return ResponseEntity.ok(subscriptions);
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponseDto> addSubscription(@Valid @RequestBody SubscriptionRequestDto requestDto) {
        SubscriptionResponseDto createdSubscription = subscriptionService.addSubscription(requestDto);
        return new ResponseEntity<>(createdSubscription, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponseDto> updateSubscription(@PathVariable Long id, @Valid @RequestBody SubscriptionRequestDto requestDto) {
        SubscriptionResponseDto updatedSubscription = subscriptionService.updateSubscription(id, requestDto);
        return ResponseEntity.ok(updatedSubscription);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteSubscription(@PathVariable Long id) {
        subscriptionService.deleteSubscription(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
