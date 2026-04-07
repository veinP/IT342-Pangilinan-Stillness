package com.stillness.payment;

import java.math.BigDecimal;

public interface PaymentStrategy {
    boolean supports(BigDecimal amount);

    PaymentResult processPayment(Long userId, BigDecimal amount, String paymentMethodId);
}