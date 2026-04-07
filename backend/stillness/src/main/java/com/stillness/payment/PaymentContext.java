package com.stillness.payment;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentContext {

    public PaymentResult executePayment(Long userId, BigDecimal amount, String paymentMethodId) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required");
        }

        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return PaymentResult.free(amount);
        }

        String reference = "PAY-" + UUID.randomUUID();
        return PaymentResult.success(reference, amount, paymentMethodId);
    }
}
