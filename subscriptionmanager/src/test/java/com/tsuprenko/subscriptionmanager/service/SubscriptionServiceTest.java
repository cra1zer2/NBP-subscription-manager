package com.tsuprenko.subscriptionmanager.service;

import com.tsuprenko.subscriptionmanager.client.NbpClient;
import com.tsuprenko.subscriptionmanager.domain.Subscription;
import com.tsuprenko.subscriptionmanager.dto.SubscriptionRequest;
import com.tsuprenko.subscriptionmanager.dto.SubscriptionResponse;
import com.tsuprenko.subscriptionmanager.exception.ResourceNotFoundException;
import com.tsuprenko.subscriptionmanager.mapper.SubscriptionMapper;
import com.tsuprenko.subscriptionmanager.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository repository;

    @Mock
    private NbpClient nbpClient;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private Subscription testSub;
    private SubscriptionRequest testRequest;
    private SubscriptionResponse testResponse;

    @BeforeEach
    void setUp() {
        testSub = Subscription.builder()
                .id(1L)
                .providerName("Netflix")
                .amount(new BigDecimal("10.00"))
                .currency("USD")
                .nextPaymentDate(LocalDate.now().plusDays(10))
                .build();

        testRequest = new SubscriptionRequest(
                "Spotify",
                new BigDecimal("5.00"),
                "USD",
                LocalDate.now().plusDays(5));

        testResponse = new SubscriptionResponse(
                1L, "Netflix", new BigDecimal("10.00"), "USD", LocalDate.now().plusDays(10));
    }

    @Test
    @DisplayName("Should get all subscriptions")
    void shouldGetAllSubscriptions() {
        when(repository.findAll()).thenReturn(List.of(testSub));
        when(subscriptionMapper.toResponse(testSub)).thenReturn(testResponse);

        List<SubscriptionResponse> responses = subscriptionService.getAllSubscriptions();

        assertEquals(1, responses.size());
        assertEquals("Netflix", responses.get(0).providerName());
    }

    @Test
    @DisplayName("Should create subscription")
    void shouldCreateSubscription() {
        Subscription savedSub = Subscription.builder()
                .id(2L)
                .providerName("Spotify")
                .amount(new BigDecimal("5.00"))
                .currency("USD")
                .nextPaymentDate(testRequest.nextPaymentDate())
                .build();

        SubscriptionResponse savedResponse = new SubscriptionResponse(
                2L, "Spotify", new BigDecimal("5.00"), "USD", testRequest.nextPaymentDate());

        when(subscriptionMapper.toEntity(testRequest)).thenReturn(savedSub);
        when(repository.save(any(Subscription.class))).thenReturn(savedSub);
        when(subscriptionMapper.toResponse(savedSub)).thenReturn(savedResponse);

        SubscriptionResponse response = subscriptionService.createSubscription(testRequest);

        assertEquals("Spotify", response.providerName());
        assertEquals(2L, response.id());
    }

    @Test
    @DisplayName("Should calculate total cost correctly for a single USD subscription")
    void shouldCalculateTotalMonthlyCostCorrectly() {
        when(repository.findAll()).thenReturn(List.of(testSub));
        when(nbpClient.getExchangeRate("USD")).thenReturn(new BigDecimal("4.00"));

        BigDecimal total = subscriptionService.calculateTotalMonthlyCost();

        assertEquals(0, new BigDecimal("40.00").compareTo(total));
    }

    @Test
    @DisplayName("Should return zero when there are no subscriptions")
    void shouldReturnZeroWhenNoSubscriptionsPresent() {
        when(repository.findAll()).thenReturn(List.of());

        BigDecimal total = subscriptionService.calculateTotalMonthlyCost();

        assertEquals(0, new BigDecimal("0.00").compareTo(total));
    }

    @Test
    @DisplayName("Should update subscription")
    void shouldUpdateSubscription() {
        when(repository.findById(1L)).thenReturn(Optional.of(testSub));
        when(repository.save(any(Subscription.class))).thenReturn(testSub);
        when(subscriptionMapper.toResponse(testSub)).thenReturn(testResponse);

        SubscriptionResponse response = subscriptionService.updateSubscription(1L, testRequest);

        assertEquals("Netflix", response.providerName());
        verify(subscriptionMapper).updateEntityFromRequest(testRequest, testSub);
    }

    @Test
    @DisplayName("Should throw when updating non-existent subscription")
    void shouldThrowWhenUpdatingNonExistent() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                subscriptionService.updateSubscription(99L, testRequest));
    }

    @Test
    @DisplayName("Should delete subscription")
    void shouldDeleteSubscription() {
        when(repository.existsById(1L)).thenReturn(true);

        subscriptionService.deleteSubscription(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw when deleting non-existent subscription")
    void shouldThrowWhenDeletingNonExistent() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                subscriptionService.deleteSubscription(99L));

        verify(repository, never()).deleteById(anyLong());
    }
}