package com.stillness.payment;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FreeBookingStrategy implements PaymentStrategy {

    @Override
    public boolean supports(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public PaymentResult processPayment(Long userId, BigDecimal amount, String paymentMethodId) {
        return PaymentResult.free(amount);
    }
}