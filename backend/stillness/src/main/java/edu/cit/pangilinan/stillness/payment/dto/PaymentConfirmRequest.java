package edu.cit.pangilinan.stillness.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConfirmRequest {

    @NotBlank(message = "Payment intent ID is required")
    private String paymentIntentId;
}
