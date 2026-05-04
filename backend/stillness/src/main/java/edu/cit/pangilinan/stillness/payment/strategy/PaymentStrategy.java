package edu.cit.pangilinan.stillness.payment.strategy;

import java.math.BigDecimal;

public interface PaymentStrategy {
    boolean supports(BigDecimal amount);

    PaymentResult processPayment(Long userId, BigDecimal amount, String paymentMethodId);
}