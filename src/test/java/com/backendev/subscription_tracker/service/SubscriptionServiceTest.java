package com.backendev.subscription_tracker.service;

import com.backendev.subscription_tracker.dto.SubscriptionRequestDto;
import com.backendev.subscription_tracker.dto.SubscriptionResponseDto;
import com.backendev.subscription_tracker.entity.Category;
import com.backendev.subscription_tracker.entity.Subscription;
import com.backendev.subscription_tracker.exception.SubscriptionAlreadyExistsException;
import com.backendev.subscription_tracker.exception.SubscriptionNotFoundException;
import com.backendev.subscription_tracker.mapper.SubscriptionMapper;
import com.backendev.subscription_tracker.repository.SubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionMapper mapper;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private SubscriptionRequestDto sampleRequest(String name) {
        return new SubscriptionRequestDto(
                name,
                new BigDecimal("15.99"),
                LocalDate.now().plusDays(10),
                Category.STREAMING);
    }

    private SubscriptionResponseDto sampleResponse(Long id, String name) {
        return new SubscriptionResponseDto(
                id,
                name,
                new BigDecimal("15.99"),
                LocalDate.now().plusDays(10),
                Category.STREAMING);
    }

    // ---------- addSubscription ----------

    @Test
    @DisplayName("addSubscription should save and return the mapped response DTO")
    void addSubscription_whenValid_shouldSaveAndReturnDto() {
        SubscriptionRequestDto request = sampleRequest("Netflix");
        Subscription entity = new Subscription();
        Subscription savedEntity = new Subscription();
        savedEntity.setId(1L);
        SubscriptionResponseDto responseDto = sampleResponse(1L, "Netflix");

        when(subscriptionRepository.existsByName("Netflix")).thenReturn(false);
        when(mapper.mapToEntity(request)).thenReturn(entity);
        when(subscriptionRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.mapToDto(savedEntity)).thenReturn(responseDto);

        SubscriptionResponseDto result = subscriptionService.addSubscription(request);

        assertEquals(1L, result.id());
        assertEquals("Netflix", result.name());
        verify(subscriptionRepository).save(entity);
    }

    @Test
    @DisplayName("addSubscription should throw SubscriptionAlreadyExistsException when name already exists")
    void addSubscription_whenDuplicate_shouldThrow() {
        SubscriptionRequestDto request = sampleRequest("Netflix");
        when(subscriptionRepository.existsByName("Netflix")).thenReturn(true);

        assertThrows(SubscriptionAlreadyExistsException.class,
                () -> subscriptionService.addSubscription(request));

        verify(subscriptionRepository, never()).save(any());
    }

    // ---------- updateSubscription ----------

    @Test
    @DisplayName("updateSubscription should mutate the loaded entity and return the mapped DTO")
    void updateSubscription_whenExists_shouldUpdateAndReturnDto() {
        Subscription existing = new Subscription(
                1L, "Netflix", new BigDecimal("15.99"),
                LocalDate.now().plusDays(10), Category.STREAMING);
        SubscriptionRequestDto request = new SubscriptionRequestDto(
                "Netflix Premium",
                new BigDecimal("24.99"),
                LocalDate.now().plusDays(30),
                Category.STREAMING);
        SubscriptionResponseDto responseDto = new SubscriptionResponseDto(
                1L, "Netflix Premium", new BigDecimal("24.99"),
                LocalDate.now().plusDays(30), Category.STREAMING);

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(mapper.mapToDto(existing)).thenReturn(responseDto);

        SubscriptionResponseDto result = subscriptionService.updateSubscription(1L, request);

        assertEquals("Netflix Premium", result.name());
        assertEquals(new BigDecimal("24.99"), result.price());
        // dirty-checking means no explicit save() call is required
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateSubscription should throw SubscriptionNotFoundException when id is missing")
    void updateSubscription_whenNotFound_shouldThrow() {
        when(subscriptionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(SubscriptionNotFoundException.class,
                () -> subscriptionService.updateSubscription(99L, sampleRequest("Ghost")));
    }

    // ---------- deleteSubscription ----------

    @Test
    @DisplayName("deleteSubscription should delete when id exists")
    void deleteSubscription_whenExists_shouldDelete() {
        Subscription existing = new Subscription();
        existing.setId(1L);
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(existing));

        subscriptionService.deleteSubscription(1L);

        verify(subscriptionRepository).delete(existing);
    }

    @Test
    @DisplayName("deleteSubscription should throw SubscriptionNotFoundException when id is missing")
    void deleteSubscription_whenNotFound_shouldThrow() {
        when(subscriptionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(SubscriptionNotFoundException.class,
                () -> subscriptionService.deleteSubscription(99L));

        verify(subscriptionRepository, never()).delete(any());
    }

    // ---------- totalMonthlyCost ----------

    @Test
    @DisplayName("totalMonthlyCost should sum the prices of all subscriptions")
    void totalMonthlyCost_shouldSumAllPrices() {
        Subscription a = new Subscription(1L, "A", new BigDecimal("10.00"), LocalDate.now().plusDays(1), null);
        Subscription b = new Subscription(2L, "B", new BigDecimal("5.50"), LocalDate.now().plusDays(1), null);
        Subscription c = new Subscription(3L, "C", new BigDecimal("0.49"), LocalDate.now().plusDays(1), null);
        when(subscriptionRepository.findAll()).thenReturn(List.of(a, b, c));

        BigDecimal total = subscriptionService.totalMonthlyCost();

        assertEquals(new BigDecimal("15.99"), total);
    }

    // ---------- getUpcomingRenewals ----------

    @Test
    @DisplayName("getUpcomingRenewals should delegate to the date-range query")
    void getUpcomingRenewals_shouldQueryDateRange() {
        Subscription near = new Subscription(1L, "Near", new BigDecimal("10.00"),
                LocalDate.now().plusDays(3), Category.STREAMING);
        SubscriptionResponseDto nearDto = new SubscriptionResponseDto(
                1L, "Near", new BigDecimal("10.00"),
                LocalDate.now().plusDays(3), Category.STREAMING);

        when(subscriptionRepository.findByRenewalDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(near));
        when(mapper.mapToDto(near)).thenReturn(nearDto);

        List<SubscriptionResponseDto> result = subscriptionService.getUpcomingRenewals();

        assertEquals(1, result.size());
        assertEquals("Near", result.get(0).name());
        verify(subscriptionRepository).findByRenewalDateBetween(any(LocalDate.class), any(LocalDate.class));
    }
}