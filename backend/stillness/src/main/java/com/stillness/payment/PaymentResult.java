package com.stillness.payment;

import java.math.BigDecimal;

public class PaymentResult {

    private final boolean successful;
    private final String paymentReference;
    private final BigDecimal amount;
    private final String paymentMethodId;

    public PaymentResult(boolean successful, String paymentReference, BigDecimal amount, String paymentMethodId) {
        this.successful = successful;
        this.paymentReference = paymentReference;
        this.amount = amount;
        this.paymentMethodId = paymentMethodId;
    }

    public static PaymentResult success(String paymentReference, BigDecimal amount, String paymentMethodId) {
        return new PaymentResult(true, paymentReference, amount, paymentMethodId);
    }

    public static PaymentResult free(BigDecimal amount) {
        return new PaymentResult(true, "FREE", amount, "FREE");
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }
}
