package edu.cit.pangilinan.stillness.controller;

import edu.cit.pangilinan.stillness.dto.response.ApiResponse;
import edu.cit.pangilinan.stillness.dto.response.PaymentRecordDto;
import edu.cit.pangilinan.stillness.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/payments")
public class AdminPaymentsController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public ResponseEntity<?> getAdminPayments() {
        List<PaymentRecordDto> records = paymentService.getAllPaymentsAsRecords();

        BigDecimal totalRevenue = records.stream()
                .filter((payment) -> "PAID".equalsIgnoreCase(payment.getStatus()))
                .map(payment -> payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long paidTransactions = records.stream()
                .filter((payment) -> "PAID".equalsIgnoreCase(payment.getStatus()))
                .count();

        long failedTransactions = records.stream()
                .filter((payment) -> "FAILED".equalsIgnoreCase(payment.getStatus()))
                .count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRevenue", totalRevenue);
        summary.put("paidTransactions", paidTransactions);
        summary.put("failedTransactions", failedTransactions);

        Map<String, Object> data = new HashMap<>();
        data.put("summary", summary);
        data.put("records", records);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now().toString())
                .build());
    }
}