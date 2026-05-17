package com.tsuprenko.subscriptionmanager.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionRequest(

        @NotBlank(message = "Provider is required")
        String providerName,

        @NotNull @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotBlank @Size(min = 3, max = 3, message = "Currency must be 3 letters")
        String currency,

        @NotNull(message = "Start date is required")
        @FutureOrPresent
        LocalDate nextPaymentDate

) {}