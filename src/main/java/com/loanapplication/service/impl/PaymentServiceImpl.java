package com.loanapplication.service.impl;

import com.loanapplication.dto.*;
import com.loanapplication.entities.EmiSchedule;
import com.loanapplication.entities.LoanAccount;
import com.loanapplication.entities.LoanPayment;
import com.loanapplication.entities.PenaltyCharge;
import com.loanapplication.repo.EmiSchedulerRepo;
import com.loanapplication.repo.LoanAccountRepo;
import com.loanapplication.repo.LoanPaymentRepo;
import com.loanapplication.repo.PenaltyChargeRepo;
import com.loanapplication.service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final EmiSchedulerRepo emiSchedulerRepo;
    private final PenaltyChargeRepo penaltyChargeRepo;
    private final LoanAccountRepo loanAccountRepo;
    private final LoanPaymentRepo loanPaymentRepo;
    private final RazorpayClient razorpayClient;
    public PaymentServiceImpl(
            EmiSchedulerRepo emiSchedulerRepo,
            PenaltyChargeRepo penaltyChargeRepo,
            LoanAccountRepo loanAccountRepo,
            LoanPaymentRepo loanPaymentRepo,
            RazorpayClient razorpayClient) {

        this.emiSchedulerRepo = emiSchedulerRepo;
        this.penaltyChargeRepo = penaltyChargeRepo;
        this.loanAccountRepo = loanAccountRepo;
        this.loanPaymentRepo = loanPaymentRepo;
        this.razorpayClient = razorpayClient;
    }

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;




    @Override
    public List<EmiPaymentResponseDto> getPayableEmis(
            Integer loanAccountId) {

        List<EmiSchedule> emiSchedules =
                emiSchedulerRepo.findByLoanAccountLoanAccountId(
                        loanAccountId
                );

        LocalDate today = LocalDate.now();

        List<EmiPaymentResponseDto> payableEmis =
                new ArrayList<>();

        for (EmiSchedule emi : emiSchedules) {


            if ("PAID".equalsIgnoreCase(
                    emi.getPaymentStatus())) {

                continue;
            }


            if (emi.getDueDate().isAfter(today)) {

                continue;
            }

            BigDecimal penalty = BigDecimal.ZERO;


            if ("BOUNCED".equalsIgnoreCase(
                    emi.getPaymentStatus())) {

                penalty = BigDecimal.valueOf(650);
            }


            else if (emi.getDueDate().isBefore(today)) {

                long lateDays =
                        ChronoUnit.DAYS.between(
                                emi.getDueDate(),
                                today
                        );

                penalty = BigDecimal.valueOf(lateDays);
            }

            EmiPaymentResponseDto dto =
                    new EmiPaymentResponseDto();

            dto.setEmiScheduleId(
                    emi.getEmiScheduleId()
            );

            dto.setInstallmentNo(
                    emi.getInstallmentNo()
            );

            dto.setDueDate(
                    emi.getDueDate()
            );

            dto.setEmiAmount(
                    emi.getEmi()
            );

            dto.setPenaltyAmount(
                    penalty
            );

            dto.setTotalPayable(
                    emi.getEmi().add(penalty)
            );

            dto.setPaymentStatus(
                    emi.getPaymentStatus()
            );

            dto.setCanPay(true);

            payableEmis.add(dto);
        }

        return payableEmis;
    }




    @Override
    public PaymentPreviewResponseDto getPaymentPreview(
            Integer loanAccountId) {

        List<EmiPaymentResponseDto> payableEmis =
                getPayableEmis(loanAccountId);

        BigDecimal totalEmiAmount =
                BigDecimal.ZERO;

        BigDecimal totalPenaltyAmount =
                BigDecimal.ZERO;

        for (EmiPaymentResponseDto emi :
                payableEmis) {

            totalEmiAmount =
                    totalEmiAmount.add(
                            emi.getEmiAmount()
                    );

            totalPenaltyAmount =
                    totalPenaltyAmount.add(
                            emi.getPenaltyAmount()
                    );
        }

        PaymentPreviewResponseDto response =
                new PaymentPreviewResponseDto();

        response.setLoanAccountId(
                loanAccountId
        );

        response.setPayableEmis(
                payableEmis
        );

        response.setTotalEmiAmount(
                totalEmiAmount
        );

        response.setTotalPenaltyAmount(
                totalPenaltyAmount
        );

        response.setTotalPayableAmount(
                totalEmiAmount.add(
                        totalPenaltyAmount
                )
        );

        return response;
    }




    @Override
    public PaymentOrderResponseDto createPaymentOrder(
            Integer loanAccountId) {

        PaymentPreviewResponseDto preview =
                getPaymentPreview(loanAccountId);

        if (preview.getPayableEmis() == null ||
                preview.getPayableEmis().isEmpty()) {

            throw new RuntimeException(
                    "No payable EMI found"
            );
        }

        BigDecimal amount =
                preview.getTotalPayableAmount();

        try {

            long amountInPaise =
                    amount
                            .multiply(
                                    BigDecimal.valueOf(100)
                            )
                            .longValue();

            JSONObject orderRequest =
                    new JSONObject();

            orderRequest.put(
                    "amount",
                    amountInPaise
            );

            orderRequest.put(
                    "currency",
                    "INR"
            );

            orderRequest.put(
                    "receipt",
                    "LOAN_" +
                            loanAccountId +
                            "_" +
                            System.currentTimeMillis()
            );

            Order order =
                    razorpayClient.orders.create(
                            orderRequest
                    );

            PaymentOrderResponseDto response =
                    new PaymentOrderResponseDto();

            response.setLoanAccountId(
                    loanAccountId
            );

            response.setAmount(
                    amount
            );

            response.setRazorpayOrderId(
                    order.get("id")
            );

            response.setCurrency(
                    "INR"
            );

            response.setPaymentStatus(
                    "CREATED"
            );

            return response;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to create Razorpay order: "
                            + e.getMessage()
            );
        }
    }




    @Override
    @Transactional
    public PaymentResponseDto verifyPayment(
            PaymentVerificationRequestDto request) {

        try {



            if (loanPaymentRepo
                    .findByRazorpayPaymentId(
                            request.getRazorpayPaymentId()
                    )
                    .isPresent()) {

                throw new RuntimeException(
                        "Payment already processed"
                );
            }



            LoanAccount loanAccount =
                    loanAccountRepo
                            .findById(
                                    request.getLoanAccountId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Loan account not found"
                                    )
                            );



            JSONObject attributes =
                    new JSONObject();

            attributes.put(
                    "razorpay_order_id",
                    request.getRazorpayOrderId()
            );

            attributes.put(
                    "razorpay_payment_id",
                    request.getRazorpayPaymentId()
            );

            attributes.put(
                    "razorpay_signature",
                    request.getRazorpaySignature()
            );




            com.razorpay.Utils.verifyPaymentSignature(
                    attributes,
                    razorpayKeySecret
            );




            PaymentPreviewResponseDto preview =
                    getPaymentPreview(
                            request.getLoanAccountId()
                    );

            if (preview.getPayableEmis() == null ||
                    preview.getPayableEmis().isEmpty()) {

                throw new RuntimeException(
                        "No payable EMI found"
                );
            }




            LoanPayment loanPayment =
                    new LoanPayment();

            loanPayment.setLoanAccount(
                    loanAccount
            );

            loanPayment.setPaymentAmount(
                    preview.getTotalPayableAmount()
            );

            loanPayment.setPaymentDate(
                    LocalDateTime.now()
            );

            loanPayment.setPaymentStatus(
                    "SUCCESS"
            );

            loanPayment.setPaymentName(
                    "EMI PAYMENT"
            );

            loanPayment.setRazorpayOrderId(
                    request.getRazorpayOrderId()
            );

            loanPayment.setRazorpayPaymentId(
                    request.getRazorpayPaymentId()
            );

            loanPaymentRepo.save(
                    loanPayment
            );




            BigDecimal principalPaid =
                    BigDecimal.ZERO;

            for (EmiPaymentResponseDto emiDto :
                    preview.getPayableEmis()) {

                EmiSchedule emi =
                        emiSchedulerRepo
                                .findById(
                                        emiDto.getEmiScheduleId()
                                )
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "EMI schedule not found"
                                        )
                                );


                emi.setPaymentStatus(
                        "PAID"
                );

                emi.setPaidDate(
                        LocalDateTime.now()
                );

                emiSchedulerRepo.save(
                        emi
                );


                if (emi.getPrincipalAmount() != null) {

                    principalPaid =
                            principalPaid.add(
                                    emi.getPrincipalAmount()
                            );
                }




                if (emiDto.getPenaltyAmount()
                        .compareTo(
                                BigDecimal.ZERO
                        ) > 0) {

                    PenaltyCharge penaltyCharge =
                            new PenaltyCharge();

                    penaltyCharge.setEmiSchedule(
                            emi
                    );

                    penaltyCharge.setLoanAccount(
                            loanAccount
                    );

                    penaltyCharge.setPenaltyAmount(
                            emiDto.getPenaltyAmount()
                    );

                    penaltyCharge.setReason(
                            "Late EMI Payment"
                    );

                    penaltyCharge.setStatus(
                            "PAID"
                    );

                    penaltyCharge.setCreatedAt(
                            LocalDateTime.now()
                    );

                    penaltyChargeRepo.save(
                            penaltyCharge
                    );
                }
            }




            BigDecimal currentTotalPaid =
                    loanAccount.getTotalPaidAmount();

            if (currentTotalPaid == null) {

                currentTotalPaid =
                        BigDecimal.ZERO;
            }

            loanAccount.setTotalPaidAmount(
                    currentTotalPaid.add(
                            preview.getTotalPayableAmount()
                    )
            );




            BigDecimal outstandingPrincipal =
                    loanAccount.getOutstandingPrincipal();

            if (outstandingPrincipal != null) {

                outstandingPrincipal =
                        outstandingPrincipal.subtract(
                                principalPaid
                        );

                if (outstandingPrincipal.compareTo(
                        BigDecimal.ZERO
                ) < 0) {

                    outstandingPrincipal =
                            BigDecimal.ZERO;
                }

                loanAccount.setOutstandingPrincipal(
                        outstandingPrincipal
                );
            }




            loanAccountRepo.save(
                    loanAccount
            );




            PaymentResponseDto response =
                    new PaymentResponseDto();

            response.setLoanAccountId(
                    request.getLoanAccountId()
            );

            response.setPaymentAmount(
                    preview.getTotalPayableAmount()
            );

            response.setRazorpayPaymentId(
                    request.getRazorpayPaymentId()
            );

            response.setPaymentStatus(
                    "SUCCESS"
            );

            return response;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Payment verification failed: "
                            + e.getMessage()
            );
        }
    }
    @Override
    public Page<PaymentHistoryResponseDto> getPaymentHistory(
            Integer loanAccountId,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "paymentDate")
        );

        Page<LoanPayment> payments =
                loanPaymentRepo.findByLoanAccountLoanAccountId(
                        loanAccountId,
                        pageable
                );

        return payments.map(payment -> {

            PaymentHistoryResponseDto dto =
                    new PaymentHistoryResponseDto();

            dto.setPaymentId(payment.getPaymentId());
            dto.setPaymentAmount(payment.getPaymentAmount());
            dto.setPaymentDate(payment.getPaymentDate());
            dto.setPaymentStatus(payment.getPaymentStatus());
            dto.setPaymentName(payment.getPaymentName());
            dto.setRazorpayOrderId(payment.getRazorpayOrderId());
            dto.setRazorpayPaymentId(payment.getRazorpayPaymentId());

            return dto;
        });
    }
}