package com.tsuprenko.subscriptionmanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionResponse(
                Long id,
                String providerName,
                BigDecimal amount,
                String currency,
                LocalDate nextPaymentDate) {
}
