package com.tsuprenko.subscriptionmanager.service;

import com.tsuprenko.subscriptionmanager.client.NbpClient;
import com.tsuprenko.subscriptionmanager.domain.Subscription;
import com.tsuprenko.subscriptionmanager.dto.SubscriptionRequest;
import com.tsuprenko.subscriptionmanager.dto.SubscriptionResponse;
import com.tsuprenko.subscriptionmanager.exception.ResourceNotFoundException;
import com.tsuprenko.subscriptionmanager.mapper.SubscriptionMapper;
import com.tsuprenko.subscriptionmanager.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Service managing subscription CRUD operations and total cost calculation.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final NbpClient nbpClient;
    private final SubscriptionMapper subscriptionMapper;

    public List<SubscriptionResponse> getAllSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .map(subscriptionMapper::toResponse)
                .toList();
    }

    public SubscriptionResponse createSubscription(SubscriptionRequest request) {
        Subscription subscription = subscriptionMapper.toEntity(request);
        subscription.setCurrency(subscription.getCurrency().toUpperCase());
        Subscription saved = subscriptionRepository.save(subscription);
        return subscriptionMapper.toResponse(saved);
    }

    /**
     * Calculates the total monthly cost of all subscriptions converted to PLN.
     */
    public BigDecimal calculateTotalMonthlyCost() {
        BigDecimal total = subscriptionRepository.findAll().stream()
                .map(sub -> {
                    BigDecimal rate = nbpClient.getExchangeRate(sub.getCurrency());
                    return sub.getAmount().multiply(rate);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public SubscriptionResponse updateSubscription(Long id, SubscriptionRequest request) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        subscriptionMapper.updateEntityFromRequest(request, sub);
        sub.setCurrency(sub.getCurrency().toUpperCase());
        Subscription updated = subscriptionRepository.save(sub);
        return subscriptionMapper.toResponse(updated);
    }

    public void deleteSubscription(Long id) {
        if (!subscriptionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subscription not found with id: " + id);
        }
        subscriptionRepository.deleteById(id);
    }
}