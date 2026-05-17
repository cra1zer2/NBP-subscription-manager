package com.tsuprenko.subscriptionmanager.controller;

import com.tsuprenko.subscriptionmanager.dto.SubscriptionRequest;
import com.tsuprenko.subscriptionmanager.dto.SubscriptionResponse;
import com.tsuprenko.subscriptionmanager.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for managing subscriptions.
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public List<SubscriptionResponse> getAll() {
        return subscriptionService.getAllSubscriptions();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse create(@Valid @RequestBody SubscriptionRequest request) {
        return subscriptionService.createSubscription(request);
    }

    @GetMapping("/total")
    public BigDecimal getTotalCost() {
        return subscriptionService.calculateTotalMonthlyCost();
    }

    @PutMapping("/{id}")
    public SubscriptionResponse update(@PathVariable Long id, @Valid @RequestBody SubscriptionRequest request) {
        return subscriptionService.updateSubscription(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        subscriptionService.deleteSubscription(id);
    }
}