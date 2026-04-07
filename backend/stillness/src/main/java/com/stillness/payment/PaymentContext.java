package com.stillness.payment;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentContext {

    private final List<PaymentStrategy> strategies;

    public PaymentContext(List<PaymentStrategy> strategies) {
        this.strategies = strategies;
    }

    public PaymentResult executePayment(Long userId, BigDecimal amount, String paymentMethodId) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required");
        }

        return strategies.stream()
                .filter(strategy -> strategy.supports(amount))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No payment strategy found for amount: " + amount))
                .processPayment(userId, amount, paymentMethodId);
    }
}
