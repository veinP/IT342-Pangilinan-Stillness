package com.stillness.payment;

import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StripePaymentStrategy implements PaymentStrategy {

    @Override
    public boolean supports(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public PaymentResult processPayment(Long userId, BigDecimal amount, String paymentMethodId) {
        try {
            PaymentIntent intent = PaymentIntent.create(
                    PaymentIntentCreateParams.builder()
                            .setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                            .setCurrency("php")
                            .setPaymentMethod(paymentMethodId)
                            .setConfirm(true)
                            .build()
            );
            return PaymentResult.success(intent.getId(), amount, paymentMethodId);
        } catch (Exception e) {
            throw new IllegalStateException("Stripe payment failed", e);
        }
    }
}