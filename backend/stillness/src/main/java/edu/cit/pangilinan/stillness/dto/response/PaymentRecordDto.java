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
public class PaymentRecordDto {
    private UUID id;
    private String bookingNumber;
    private String userName;
    private String sessionTitle;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String cardMasked;
    private String transactionId;
    private String status;
    private LocalDateTime date;
    private LocalDateTime refundDate;
    private String refundReason;
}
