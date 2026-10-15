package com.loanapplication.controllers;


import com.loanapplication.dto.*;
import com.loanapplication.helper.ApiResponse;
import com.loanapplication.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;




    @GetMapping("/emi/{loanAccountId}")
    public ResponseEntity<
            ApiResponse<List<EmiPaymentResponseDto>>> getPayableEmis(
            @PathVariable Integer loanAccountId) {

        List<EmiPaymentResponseDto> payableEmis =
                paymentService.getPayableEmis(
                        loanAccountId
                );

        ApiResponse<List<EmiPaymentResponseDto>> response =
                new ApiResponse<>(
                        true,
                        "Payable EMIs fetched successfully",
                        payableEmis,
                        null
                );

        return ResponseEntity.ok(response);
    }




    @GetMapping("/preview/{loanAccountId}")
    public ResponseEntity<
            ApiResponse<PaymentPreviewResponseDto>> getPaymentPreview(
            @PathVariable Integer loanAccountId) {

        PaymentPreviewResponseDto preview =
                paymentService.getPaymentPreview(
                        loanAccountId
                );

        ApiResponse<PaymentPreviewResponseDto> response =
                new ApiResponse<>(
                        true,
                        "Payment preview fetched successfully",
                        preview,
                        null
                );

        return ResponseEntity.ok(response);
    }




    @PostMapping("/order/{loanAccountId}")
    public ResponseEntity<
            ApiResponse<PaymentOrderResponseDto>> createPaymentOrder(
            @PathVariable Integer loanAccountId) {

        PaymentOrderResponseDto order =
                paymentService.createPaymentOrder(
                        loanAccountId
                );

        ApiResponse<PaymentOrderResponseDto> response =
                new ApiResponse<>(
                        true,
                        "Razorpay order created successfully",
                        order,
                        null
                );

        return ResponseEntity.ok(response);
    }




    @PostMapping("/verify")
    public ResponseEntity<
            ApiResponse<PaymentResponseDto>> verifyPayment(
            @RequestBody PaymentVerificationRequestDto request) {

        PaymentResponseDto payment =
                paymentService.verifyPayment(
                        request
                );

        ApiResponse<PaymentResponseDto> response =
                new ApiResponse<>(
                        true,
                        "Payment verified successfully",
                        payment,
                        null
                );

        return ResponseEntity.ok(response);
    }
    @GetMapping("/history/{loanAccountId}")
    public ResponseEntity<ApiResponse<Page<PaymentHistoryResponseDto>>> getPaymentHistory(
            @PathVariable Integer loanAccountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<PaymentHistoryResponseDto> history =
                paymentService.getPaymentHistory(
                        loanAccountId,
                        page,
                        size
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Payment history fetched successfully",
                        history,
                        null
                )
        );
    }

    @PostMapping("/part-payment/order/{foreClosureId}")
    public ResponseEntity<ApiResponse<PaymentOrderResponseDto>>
    createPartPaymentOrder(
            @PathVariable Integer foreClosureId) {

        PaymentOrderResponseDto order =
                paymentService.createPartPaymentOrder(foreClosureId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Part-payment order created successfully",
                        order,
                        null
                )
        );
    }

    @PostMapping("/part-payment/verify/{foreClosureId}")
    public ResponseEntity<ApiResponse<PaymentResponseDto>>
    verifyPartPayment(
            @PathVariable Integer foreClosureId,
            @RequestBody PaymentVerificationRequestDto request) {

        PaymentResponseDto payment =
                paymentService.verifyPartPayment(
                        foreClosureId,
                        request
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Part-payment verified successfully",
                        payment,
                        null
                )
        );
    }

}