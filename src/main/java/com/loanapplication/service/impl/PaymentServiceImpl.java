 package com.loanapplication.service.impl;

import com.loanapplication.dto.*;
import com.loanapplication.entities.*;
import com.loanapplication.repo.*;
import com.loanapplication.service.LoanClosureService;
import com.loanapplication.service.PaymentService;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

import lombok.extern.slf4j.Slf4j;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final EmiSchedulerRepo emiSchedulerRepo;
    private final PenaltyChargeRepo penaltyChargeRepo;
    private final LoanAccountRepo loanAccountRepo;
    private final LoanPaymentRepo loanPaymentRepo;
    private final RazorpayPaymentOrderRepo razorpayPaymentOrderRepo;
    private final RazorpayClient razorpayClient;
    private final ForeClosureRequestRepo foreClosureRequestRepo;
    private final LoanClosureService loanClosureService;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public PaymentServiceImpl(
            EmiSchedulerRepo emiSchedulerRepo,
            PenaltyChargeRepo penaltyChargeRepo,
            LoanAccountRepo loanAccountRepo,
            LoanPaymentRepo loanPaymentRepo,
            RazorpayPaymentOrderRepo razorpayPaymentOrderRepo,
            RazorpayClient razorpayClient,
            ForeClosureRequestRepo foreClosureRequestRepo,
            LoanClosureService loanClosureService) {

        this.emiSchedulerRepo = emiSchedulerRepo;
        this.penaltyChargeRepo = penaltyChargeRepo;
        this.loanAccountRepo = loanAccountRepo;
        this.loanPaymentRepo = loanPaymentRepo;
        this.razorpayPaymentOrderRepo = razorpayPaymentOrderRepo;
        this.razorpayClient = razorpayClient;
        this.foreClosureRequestRepo = foreClosureRequestRepo;
        this.loanClosureService = loanClosureService;
    }

    // ---------------------------------------------------------
    // 1. GET PAYABLE EMIs
    // ---------------------------------------------------------

    @Override
    public List<EmiPaymentResponseDto> getPayableEmis(
            Integer loanAccountId) {

        loanAccountRepo.findById(loanAccountId)
                .orElseThrow(() ->
                        new RuntimeException("Loan account not found"));

        List<EmiSchedule> emiSchedules =
                emiSchedulerRepo.findByLoanAccountLoanAccountId(
                        loanAccountId);

        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);

        List<EmiPaymentResponseDto> payableEmis =
                new ArrayList<>();

        for (EmiSchedule emi : emiSchedules) {

            String status = emi.getPaymentStatus();

            if ("PAID".equalsIgnoreCase(status)) {
                continue;
            }

            if (!"PENDING".equalsIgnoreCase(status)
                    && !"UNPAID".equalsIgnoreCase(status)
                    && !"BOUNCED".equalsIgnoreCase(status)) {
                continue;
            }

            if (emi.getDueDate() == null
                    || emi.getEmi() == null) {
                continue;
            }

            YearMonth emiMonth =
                    YearMonth.from(emi.getDueDate());

            // An EMI becomes payable at the start of its month.
            if (emiMonth.isAfter(currentMonth)) {
                continue;
            }

            BigDecimal penalty = BigDecimal.ZERO;

            // Bounce charge: Rs. 650.
            if ("BOUNCED".equalsIgnoreCase(status)) {
                penalty = penalty.add(
                        BigDecimal.valueOf(650));
            }

            // Late charge: Rs. 1 per overdue day.
            if (emi.getDueDate().isBefore(today)) {

                long lateDays = ChronoUnit.DAYS.between(
                        emi.getDueDate(), today);

                penalty = penalty.add(
                        BigDecimal.valueOf(lateDays));
            }

            EmiPaymentResponseDto dto =
                    new EmiPaymentResponseDto();

            dto.setEmiScheduleId(emi.getEmiScheduleId());
            dto.setInstallmentNo(emi.getInstallmentNo());
            dto.setDueDate(emi.getDueDate());
            dto.setEmiAmount(emi.getEmi());
            dto.setPenaltyAmount(penalty);
            dto.setTotalPayable(emi.getEmi().add(penalty));
            dto.setPaymentStatus(status);
            dto.setCanPay(true);

            payableEmis.add(dto);
        }

        return payableEmis;
    }

    // ---------------------------------------------------------
    // 2. PAYMENT PREVIEW
    // ---------------------------------------------------------

    @Override
    public PaymentPreviewResponseDto getPaymentPreview(
            Integer loanAccountId) {

        List<EmiPaymentResponseDto> payableEmis =
                getPayableEmis(loanAccountId);

        BigDecimal totalEmiAmount = BigDecimal.ZERO;
        BigDecimal totalPenaltyAmount = BigDecimal.ZERO;

        for (EmiPaymentResponseDto emi : payableEmis) {

            totalEmiAmount = totalEmiAmount.add(
                    emi.getEmiAmount());

            totalPenaltyAmount = totalPenaltyAmount.add(
                    emi.getPenaltyAmount());
        }

        PaymentPreviewResponseDto response =
                new PaymentPreviewResponseDto();

        response.setLoanAccountId(loanAccountId);
        response.setPayableEmis(payableEmis);
        response.setTotalEmiAmount(totalEmiAmount);
        response.setTotalPenaltyAmount(totalPenaltyAmount);

        response.setTotalPayableAmount(
                totalEmiAmount.add(totalPenaltyAmount));

        return response;
    }

    // ---------------------------------------------------------
    // 3. CREATE RAZORPAY ORDER AND SAVE EMI SNAPSHOT
    // ---------------------------------------------------------

    @Override
    @Transactional
    public PaymentOrderResponseDto createPaymentOrder(
            Integer loanAccountId) {

        LoanAccount loanAccount =
                loanAccountRepo.findById(loanAccountId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Loan account not found"));

        PaymentPreviewResponseDto preview =
                getPaymentPreview(loanAccountId);

        if (preview.getPayableEmis() == null
                || preview.getPayableEmis().isEmpty()) {
            throw new RuntimeException("No payable EMI found");
        }

        BigDecimal amount =
                preview.getTotalPayableAmount()
                        .setScale(2, RoundingMode.HALF_UP);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(
                    "Payment amount must be greater than zero");
        }

        // Store the exact EMI breakdown as JSON.
        JSONArray emiSnapshot = new JSONArray();

        for (EmiPaymentResponseDto emi : preview.getPayableEmis()) {

            JSONObject item = new JSONObject();

            item.put("emiScheduleId", emi.getEmiScheduleId());
            item.put("emiAmount", emi.getEmiAmount());
            item.put("penaltyAmount", emi.getPenaltyAmount());
            item.put("originalStatus", emi.getPaymentStatus());

            emiSnapshot.put(item);
        }

        try {

            long amountInPaise = amount
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();

            JSONObject orderRequest = new JSONObject();

            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put(
                    "receipt",
                    "LOAN_" + loanAccountId
                            + "_" + System.currentTimeMillis());

            Order order =
                    razorpayClient.orders.create(orderRequest);

            RazorpayPaymentOrder savedOrder =
                    new RazorpayPaymentOrder();

            savedOrder.setLoanAccount(loanAccount);
            savedOrder.setRazorpayOrderId(order.get("id"));
            savedOrder.setAmount(amount);
            savedOrder.setCurrency("INR");
            savedOrder.setOrderStatus("CREATED");
            savedOrder.setCreatedAt(LocalDateTime.now());
            savedOrder.setEmiSnapshot(emiSnapshot.toString());

            razorpayPaymentOrderRepo.save(savedOrder);

            PaymentOrderResponseDto response =
                    new PaymentOrderResponseDto();

            response.setLoanAccountId(loanAccountId);
            response.setAmount(amount);
            response.setRazorpayOrderId(order.get("id"));
            response.setCurrency("INR");
            response.setPaymentStatus("CREATED");

            return response;

        } catch (Exception e) {

            log.error(
                    "Failed to create Razorpay order for loan {}",
                    loanAccountId, e);

            throw new RuntimeException(
                    "Failed to create Razorpay order: "
                            + e.getMessage(), e);
        }
    }
    @Override
    @Transactional
    public PaymentOrderResponseDto createPartPaymentOrder(
            Integer foreclosureRequestId) {

        ForeClosureRequest foreclosureRequest =
                foreClosureRequestRepo.findById(foreclosureRequestId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Foreclosure request not found"));

        if (!"PARTIAL".equalsIgnoreCase(
                foreclosureRequest.getForeclosureType())) {
            throw new RuntimeException(
                    "This is not a part-payment request");
        }

        if (!"APPROVED".equalsIgnoreCase(
                foreclosureRequest.getStatus())) {
            throw new RuntimeException(
                    "Part-payment request is not approved");
        }

        if (Boolean.TRUE.equals(foreclosureRequest.getPaid())) {
            throw new RuntimeException(
                    "Part-payment has already been completed");
        }

        LoanAccount loanAccount = foreclosureRequest.getLoanAccount();

        BigDecimal amount = foreclosureRequest.getPartialAmount()
                .setScale(2, RoundingMode.HALF_UP);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(
                    "Part-payment amount must be greater than zero");
        }

        if (loanAccount.getOutstandingPrincipal() == null
                || amount.compareTo(
                loanAccount.getOutstandingPrincipal()) >= 0) {
            throw new RuntimeException(
                    "Part-payment must be less than outstanding principal");
        }

        try {
            long amountInPaise = amount
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put(
                    "receipt",
                    "PART_" + foreclosureRequestId
                            + "_" + System.currentTimeMillis());

            Order razorpayOrder =
                    razorpayClient.orders.create(orderRequest);

            RazorpayPaymentOrder savedOrder =
                    new RazorpayPaymentOrder();

            savedOrder.setLoanAccount(loanAccount);
            savedOrder.setRazorpayOrderId(razorpayOrder.get("id"));
            savedOrder.setAmount(amount);
            savedOrder.setCurrency("INR");
            savedOrder.setOrderStatus("CREATED");
            savedOrder.setCreatedAt(LocalDateTime.now());
            savedOrder.setPaymentPurpose("PART_PAYMENT");
            savedOrder.setForeClosureRequest(foreclosureRequest);
            savedOrder.setEmiSnapshot(null);

            razorpayPaymentOrderRepo.save(savedOrder);

            PaymentOrderResponseDto response =
                    new PaymentOrderResponseDto();

            response.setLoanAccountId(
                    loanAccount.getLoanAccountId());
            response.setAmount(amount);
            response.setRazorpayOrderId(razorpayOrder.get("id"));
            response.setCurrency("INR");
            response.setPaymentStatus("CREATED");

            return response;

        } catch (Exception e) {
            log.error(
                    "Failed to create part-payment order for request {}",
                    foreclosureRequestId, e);

            throw new RuntimeException(
                    "Failed to create part-payment order: "
                            + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------
    // 4. VERIFY PAYMENT AND PROCESS SAVED EMI SNAPSHOT
    // ---------------------------------------------------------

    @Override
    @Transactional
    public PaymentResponseDto verifyPayment(
            PaymentVerificationRequestDto request) {

        try {

            if (request == null
                    || request.getLoanAccountId() == null
                    || request.getRazorpayOrderId() == null
                    || request.getRazorpayPaymentId() == null
                    || request.getRazorpaySignature() == null) {

                throw new RuntimeException(
                        "Required payment details are missing");
            }

            if (loanPaymentRepo.findByRazorpayPaymentId(
                    request.getRazorpayPaymentId()).isPresent()) {

                throw new RuntimeException(
                        "Payment already processed");
            }

            RazorpayPaymentOrder savedOrder =
                    razorpayPaymentOrderRepo
                            .findByRazorpayOrderId(
                                    request.getRazorpayOrderId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Payment order not found"));

            if (!savedOrder.getLoanAccount()
                    .getLoanAccountId()
                    .equals(request.getLoanAccountId())) {

                throw new RuntimeException(
                        "Payment order does not belong to this loan");
            }

            if (!"CREATED".equalsIgnoreCase(
                    savedOrder.getOrderStatus())) {

                throw new RuntimeException(
                        "Payment order has already been processed");
            }

            if (savedOrder.getEmiSnapshot() == null
                    || savedOrder.getEmiSnapshot().isBlank()) {

                throw new RuntimeException(
                        "EMI snapshot missing. Create a new order.");
            }

            LoanAccount loanAccount =
                    loanAccountRepo.findById(
                                    request.getLoanAccountId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Loan account not found"));

            // Verify the Razorpay signature.
            JSONObject attributes = new JSONObject();

            attributes.put(
                    "razorpay_order_id",
                    request.getRazorpayOrderId());

            attributes.put(
                    "razorpay_payment_id",
                    request.getRazorpayPaymentId());

            attributes.put(
                    "razorpay_signature",
                    request.getRazorpaySignature());

            Utils.verifyPaymentSignature(
                    attributes, razorpayKeySecret);

            // Fetch payment details directly from Razorpay.
            Payment gatewayPayment =
                    razorpayClient.payments.fetch(
                            request.getRazorpayPaymentId());

            String gatewayOrderId =
                    gatewayPayment.get("order_id");

            if (!request.getRazorpayOrderId()
                    .equals(gatewayOrderId)) {

                throw new RuntimeException(
                        "Payment does not match the order");
            }

            if (!"captured".equalsIgnoreCase(
                    gatewayPayment.get("status"))) {

                throw new RuntimeException(
                        "Payment has not been captured");
            }

            long gatewayAmount =
                    ((Number) gatewayPayment.get("amount"))
                            .longValue();

            long savedAmount =
                    savedOrder.getAmount()
                            .multiply(BigDecimal.valueOf(100))
                            .longValueExact();

            if (gatewayAmount != savedAmount) {
                throw new RuntimeException(
                        "Payment amount does not match saved order");
            }

            JSONArray snapshot =
                    new JSONArray(savedOrder.getEmiSnapshot());

            if (snapshot.isEmpty()) {
                throw new RuntimeException(
                        "No EMIs found in saved snapshot");
            }

            BigDecimal snapshotTotal = BigDecimal.ZERO;
            BigDecimal principalPaid = BigDecimal.ZERO;

            List<EmiSchedule> emisToPay = new ArrayList<>();

            /*
             * Validate the complete snapshot before changing records.
             */
            for (int i = 0; i < snapshot.length(); i++) {

                JSONObject item = snapshot.getJSONObject(i);

                int emiId = item.getInt("emiScheduleId");

                BigDecimal emiAmount =
                        item.getBigDecimal("emiAmount");

                BigDecimal penaltyAmount =
                        item.getBigDecimal("penaltyAmount");

                String originalStatus =
                        item.getString("originalStatus");

                EmiSchedule emi =
                        emiSchedulerRepo.findById(emiId)
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "EMI not found: " + emiId));

                if (!emi.getLoanAccount()
                        .getLoanAccountId()
                        .equals(request.getLoanAccountId())) {

                    throw new RuntimeException(
                            "EMI belongs to another loan");
                }

                if (!originalStatus.equalsIgnoreCase(
                        emi.getPaymentStatus())) {

                    throw new RuntimeException(
                            "EMI status changed. Create a new order.");
                }

                if ("PAID".equalsIgnoreCase(
                        emi.getPaymentStatus())) {

                    throw new RuntimeException(
                            "EMI has already been paid");
                }

                if (emi.getEmi() == null
                        || emi.getEmi().compareTo(emiAmount) != 0) {

                    throw new RuntimeException(
                            "EMI amount changed. Create a new order.");
                }

                if (emiAmount.compareTo(BigDecimal.ZERO) <= 0
                        || penaltyAmount.compareTo(
                        BigDecimal.ZERO) < 0) {

                    throw new RuntimeException(
                            "Invalid EMI or penalty amount");
                }

                snapshotTotal = snapshotTotal.add(
                        emiAmount.add(penaltyAmount));

                if (emi.getPrincipalAmount() != null) {
                    principalPaid = principalPaid.add(
                            emi.getPrincipalAmount());
                }

                emisToPay.add(emi);
            }

            if (snapshotTotal.compareTo(
                    savedOrder.getAmount()) != 0) {

                throw new RuntimeException(
                        "EMI snapshot total does not match order amount");
            }

            // Save successful payment history.
            LoanPayment loanPayment = new LoanPayment();

            loanPayment.setLoanAccount(loanAccount);
            loanPayment.setPaymentAmount(savedOrder.getAmount());
            loanPayment.setPaymentDate(LocalDateTime.now());
            loanPayment.setPaymentStatus("SUCCESS");
            loanPayment.setPaymentName("EMI PAYMENT");
            loanPayment.setRazorpayOrderId(
                    request.getRazorpayOrderId());
            loanPayment.setRazorpayPaymentId(
                    request.getRazorpayPaymentId());

            loanPaymentRepo.save(loanPayment);

            // Process exactly the EMIs from the saved snapshot.
            for (int i = 0; i < snapshot.length(); i++) {

                JSONObject item = snapshot.getJSONObject(i);
                EmiSchedule emi = emisToPay.get(i);

                String originalStatus =
                        item.getString("originalStatus");

                BigDecimal penaltyAmount =
                        item.getBigDecimal("penaltyAmount");

                emi.setPaymentStatus("PAID");
                emi.setPaidDate(LocalDateTime.now());

                emiSchedulerRepo.save(emi);

                if (penaltyAmount.compareTo(
                        BigDecimal.ZERO) > 0) {

                    PenaltyCharge penaltyCharge =
                            new PenaltyCharge();

                    penaltyCharge.setEmiSchedule(emi);
                    penaltyCharge.setLoanAccount(loanAccount);
                    penaltyCharge.setPenaltyAmount(penaltyAmount);

                    penaltyCharge.setReason(
                            "BOUNCED".equalsIgnoreCase(originalStatus)
                                    ? "Bounce and late payment charges"
                                    : "Late EMI Payment");

                    penaltyCharge.setStatus("PAID");
                    penaltyCharge.setCreatedAt(LocalDateTime.now());

                    penaltyChargeRepo.save(penaltyCharge);
                }
            }

            BigDecimal totalPaid =
                    loanAccount.getTotalPaidAmount();

            if (totalPaid == null) {
                totalPaid = BigDecimal.ZERO;
            }

            loanAccount.setTotalPaidAmount(
                    totalPaid.add(savedOrder.getAmount()));

            BigDecimal outstanding =
                    loanAccount.getOutstandingPrincipal();

            if (outstanding != null) {

                outstanding = outstanding.subtract(principalPaid);

                if (outstanding.compareTo(BigDecimal.ZERO) < 0) {
                    outstanding = BigDecimal.ZERO;
                }

                loanAccount.setOutstandingPrincipal(outstanding);
            }

            loanAccountRepo.save(loanAccount);

            savedOrder.setOrderStatus("PAID");
            savedOrder.setProcessedAt(LocalDateTime.now());

            razorpayPaymentOrderRepo.save(savedOrder);

            PaymentResponseDto response =
                    new PaymentResponseDto();

            response.setLoanAccountId(
                    request.getLoanAccountId());

            response.setPaymentAmount(
                    savedOrder.getAmount());

            response.setRazorpayPaymentId(
                    request.getRazorpayPaymentId());

            response.setPaymentStatus("SUCCESS");

            return response;

        } catch (Exception e) {

            log.error(
                    "Payment verification failed for order {}",
                    request != null
                            ? request.getRazorpayOrderId()
                            : null,
                    e);

            throw new RuntimeException(
                    "Payment verification failed: "
                            + e.getMessage(), e);
        }
    }
    @Override
    @Transactional
    public PaymentResponseDto verifyPartPayment(
            Integer foreclosureRequestId,
            PaymentVerificationRequestDto request) {

        try {
            if (request == null
                    || request.getLoanAccountId() == null
                    || request.getRazorpayOrderId() == null
                    || request.getRazorpayPaymentId() == null
                    || request.getRazorpaySignature() == null) {

                throw new RuntimeException(
                        "Required payment details are missing");
            }

            if (loanPaymentRepo.findByRazorpayPaymentId(
                    request.getRazorpayPaymentId()).isPresent()) {
                throw new RuntimeException(
                        "Payment already processed");
            }

            ForeClosureRequest foreclosureRequest =
                    foreClosureRequestRepo.findById(foreclosureRequestId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Foreclosure request not found"));

            if (!"PARTIAL".equalsIgnoreCase(
                    foreclosureRequest.getForeclosureType())
                    || !"APPROVED".equalsIgnoreCase(
                    foreclosureRequest.getStatus())
                    || Boolean.TRUE.equals(
                    foreclosureRequest.getPaid())) {

                throw new RuntimeException(
                        "Part-payment request is invalid or already processed");
            }

            LoanAccount loanAccount =
                    foreclosureRequest.getLoanAccount();

            if (!loanAccount.getLoanAccountId()
                    .equals(request.getLoanAccountId())) {
                throw new RuntimeException(
                        "Request does not belong to this loan");
            }

            RazorpayPaymentOrder savedOrder =
                    razorpayPaymentOrderRepo
                            .findByRazorpayOrderId(
                                    request.getRazorpayOrderId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Payment order not found"));

            if (!"PART_PAYMENT".equalsIgnoreCase(
                    savedOrder.getPaymentPurpose())) {
                throw new RuntimeException(
                        "This is not a part-payment order");
            }

            if (!"CREATED".equalsIgnoreCase(
                    savedOrder.getOrderStatus())) {
                throw new RuntimeException(
                        "Payment order has already been processed");
            }

            if (savedOrder.getForeClosureRequest() == null
                    || !savedOrder.getForeClosureRequest()
                    .getRequestId()
                    .equals(foreclosureRequestId)) {
                throw new RuntimeException(
                        "Payment order does not match the foreclosure request");
            }

            if (!savedOrder.getLoanAccount()
                    .getLoanAccountId()
                    .equals(request.getLoanAccountId())) {
                throw new RuntimeException(
                        "Payment order does not belong to this loan");
            }

            BigDecimal expectedAmount =
                    foreclosureRequest.getPartialAmount()
                            .setScale(2, RoundingMode.HALF_UP);

            if (savedOrder.getAmount().compareTo(expectedAmount) != 0) {
                throw new RuntimeException(
                        "Part-payment amount does not match the saved order");
            }

            // Verify Razorpay signature.
            JSONObject attributes = new JSONObject();
            attributes.put(
                    "razorpay_order_id",
                    request.getRazorpayOrderId());
            attributes.put(
                    "razorpay_payment_id",
                    request.getRazorpayPaymentId());
            attributes.put(
                    "razorpay_signature",
                    request.getRazorpaySignature());

            Utils.verifyPaymentSignature(
                    attributes, razorpayKeySecret);

            // Confirm payment directly with Razorpay.
            Payment gatewayPayment =
                    razorpayClient.payments.fetch(
                            request.getRazorpayPaymentId());

            if (!request.getRazorpayOrderId().equals(
                    gatewayPayment.get("order_id"))) {
                throw new RuntimeException(
                        "Payment does not match the order");
            }

            if (!"captured".equalsIgnoreCase(
                    gatewayPayment.get("status"))) {
                throw new RuntimeException(
                        "Payment has not been captured");
            }

            long gatewayAmount =
                    ((Number) gatewayPayment.get("amount")).longValue();

            long savedAmount =
                    savedOrder.getAmount()
                            .multiply(BigDecimal.valueOf(100))
                            .longValueExact();

            if (gatewayAmount != savedAmount) {
                throw new RuntimeException(
                        "Payment amount does not match saved order");
            }

            // Record successful part-payment.
            LoanPayment loanPayment = new LoanPayment();
            loanPayment.setLoanAccount(loanAccount);
            loanPayment.setPaymentAmount(savedOrder.getAmount());
            loanPayment.setPaymentDate(LocalDateTime.now());
            loanPayment.setPaymentStatus("SUCCESS");
            loanPayment.setPaymentName("PART_PAYMENT");
            loanPayment.setRazorpayOrderId(
                    request.getRazorpayOrderId());
            loanPayment.setRazorpayPaymentId(
                    request.getRazorpayPaymentId());

            loanPaymentRepo.save(loanPayment);

            // Update total amount paid.
            BigDecimal totalPaid = loanAccount.getTotalPaidAmount();

            if (totalPaid == null) {
                totalPaid = BigDecimal.ZERO;
            }

            loanAccount.setTotalPaidAmount(
                    totalPaid.add(savedOrder.getAmount()));

            loanAccountRepo.save(loanAccount);

            // Mark the order as paid.
            savedOrder.setOrderStatus("PAID");
            savedOrder.setProcessedAt(LocalDateTime.now());
            razorpayPaymentOrderRepo.save(savedOrder);

            // Mark the request paid before applying the part-payment.
            foreclosureRequest.setPaid(true);
            foreclosureRequest.setPaidDate(LocalDateTime.now());
            foreClosureRequestRepo.save(foreclosureRequest);

            /*
             * Your existing closure service subtracts the partial amount
             * from outstanding principal and regenerates future EMIs.
             */
            loanClosureService.createEmiScheduleAfterPartPayment(
                    foreclosureRequestId);

            PaymentResponseDto response = new PaymentResponseDto();
            response.setLoanAccountId(request.getLoanAccountId());
            response.setPaymentAmount(savedOrder.getAmount());
            response.setRazorpayPaymentId(
                    request.getRazorpayPaymentId());
            response.setPaymentStatus("SUCCESS");

            return response;

        } catch (Exception e) {
            log.error(
                    "Part-payment verification failed for request {}",
                    foreclosureRequestId, e);

            throw new RuntimeException(
                    "Part-payment verification failed: "
                            + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------
    // 5. PAYMENT HISTORY
    // ---------------------------------------------------------

    @Override
    public Page<PaymentHistoryResponseDto> getPaymentHistory(
            Integer loanAccountId,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "paymentDate"));

        Page<LoanPayment> payments =
                loanPaymentRepo.findByLoanAccountLoanAccountId(
                        loanAccountId, pageable);

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
