package com.tsuprenko.subscriptionmanager.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "subscriptions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Provider name cannot be empty")
    private String providerName;

    @NotNull
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    @FutureOrPresent
    private LocalDate nextPaymentDate;
}