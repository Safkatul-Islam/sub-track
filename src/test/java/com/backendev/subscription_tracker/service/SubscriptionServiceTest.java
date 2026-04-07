package com.backendev.subscription_tracker.service;

import com.backendev.subscription_tracker.dto.SubscriptionRequestDto;
import com.backendev.subscription_tracker.dto.SubscriptionResponseDto;
import com.backendev.subscription_tracker.entity.Subscription;
import com.backendev.subscription_tracker.mapper.SubscriptionMapper;
import com.backendev.subscription_tracker.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private SubscriptionMapper mapper;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    void createSubscription_WhenGivenValidDto_ShouldSaveAndReturnResponseDto() {

        // Arrange
        SubscriptionRequestDto req = new SubscriptionRequestDto();
        req.setName("Netflix");

        Subscription entity = new Subscription();
        SubscriptionResponseDto dto = new SubscriptionResponseDto();
        dto.setName("Netflix");

        when(subscriptionRepository.existsByName("Netflix")).thenReturn(false);
        when(mapper.mapToEntity(req)).thenReturn(entity);
        when(subscriptionRepository.save(entity)).thenReturn(entity);
        when(mapper.mapToDto(entity)).thenReturn(dto);

        // Act
        SubscriptionResponseDto result = subscriptionService.addSubscription(req);

        // Assert
        assertEquals("Netflix", result.getName());
        verify(subscriptionRepository).existsByName("Netflix");
        verify(subscriptionRepository).save(entity);
        verify(mapper).mapToEntity(req);
        verify(mapper).mapToDto(entity);
        verifyNoMoreInteractions(subscriptionRepository, mapper);
    }

}