package edu.cit.pangilinan.stillness.controller;

import edu.cit.pangilinan.stillness.dto.request.PaymentConfirmRequest;
import edu.cit.pangilinan.stillness.dto.response.ApiResponse;
import edu.cit.pangilinan.stillness.dto.response.PaymentDto;
import edu.cit.pangilinan.stillness.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@Valid @RequestBody PaymentConfirmRequest request) {
        try {
            // Extract bookingId from the request body if needed
            // For now, we'll use the paymentIntentId to find the payment
            PaymentDto payment = paymentService.confirmPayment(request.getPaymentIntentId(), "txn_" + System.currentTimeMillis());
            if (payment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("PAYMENT_NOT_FOUND", "Payment not found"));
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("payment", payment);
            data.put("message", "Payment confirmed successfully");
            
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .data(data)
                    .timestamp(LocalDateTime.now().toString())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("PAYMENT_CONFIRMATION_FAILED", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentById(@PathVariable UUID id) {
        try {
            PaymentDto payment = paymentService.getPaymentById(id);
            if (payment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("PAYMENT_NOT_FOUND", "Payment not found"));
            }
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .data(payment)
                    .timestamp(LocalDateTime.now().toString())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("PAYMENT_FETCH_FAILED", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserPayments(@PathVariable UUID userId) {
        try {
            // TODO: Get current user from Security Context and verify it's the same user
            List<PaymentDto> payments = paymentService.getUserPayments(null);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .data(payments)
                    .timestamp(LocalDateTime.now().toString())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("PAYMENTS_FETCH_FAILED", e.getMessage()));
        }
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<?> refundPayment(@PathVariable UUID paymentId, 
                                          @RequestParam(defaultValue = "") String reason) {
        try {
            PaymentDto payment = paymentService.refundPayment(paymentId, reason);
            if (payment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("PAYMENT_NOT_FOUND", "Payment not found"));
            }
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .data(payment)
                    .timestamp(LocalDateTime.now().toString())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("REFUND_FAILED", e.getMessage()));
        }
    }
}
