package edu.cit.pangilinan.stillness.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {
    private UUID id;
    private UUID bookingId;
    private UUID userId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String paymentIntentId;
    private String transactionId;
    private String status;
    private LocalDateTime paymentDate;
    private LocalDateTime refundDate;
    private String refundReason;
}
