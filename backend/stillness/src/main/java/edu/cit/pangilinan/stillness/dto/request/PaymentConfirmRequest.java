package edu.cit.pangilinan.stillness.dto.request;

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
