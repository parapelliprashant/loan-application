package com.loanapplication.service;

import com.loanapplication.dto.EmiPaymentResponseDto;
import com.loanapplication.dto.PaymentOrderResponseDto;
import com.loanapplication.dto.PaymentPreviewResponseDto;
import com.loanapplication.dto.PaymentResponseDto;
import com.loanapplication.dto.PaymentVerificationRequestDto;
import com.loanapplication.dto.PaymentHistoryResponseDto;

import org.springframework.data.domain.Page;
import java.util.List;

public interface PaymentService {

    List<EmiPaymentResponseDto> getPayableEmis(Integer loanAccountId);

    PaymentPreviewResponseDto getPaymentPreview(Integer loanAccountId);

    PaymentOrderResponseDto createPaymentOrder(Integer loanAccountId);

    PaymentResponseDto verifyPayment(PaymentVerificationRequestDto request);

    Page<PaymentHistoryResponseDto> getPaymentHistory(
            Integer loanAccountId,
            int page,
            int size
    );
}