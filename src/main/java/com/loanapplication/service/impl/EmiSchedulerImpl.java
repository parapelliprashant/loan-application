
package com.loanapplication.service.impl;

import com.loanapplication.dto.EmiSchedulerResponseDto;
import com.loanapplication.entities.EmiSchedule;
import com.loanapplication.entities.LoanAccount;
import com.loanapplication.repo.EmiSchedulerRepo;
import com.loanapplication.repo.LoanAccountRepo;
import com.loanapplication.service.EmiSchedluerService;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmiSchedulerImpl implements EmiSchedluerService {

    private final ModelMapper modelMapper;
    private final EmiSchedulerRepo emiSchedulerRepo;
    private final LoanAccountRepo loanAccountRepo;

    // ---------------------------------------------------------
    // 1. GENERATE THE ORIGINAL EMI SCHEDULE
    // ---------------------------------------------------------

    @Override
    @Transactional
    public List<EmiSchedulerResponseDto> generateEMiSceduleForEverymonth(
            int loanAccountId) {

        LoanAccount loanAccount = loanAccountRepo.findById(loanAccountId)
                .orElseThrow(() ->
                        new RuntimeException("Loan account not found"));

        BigDecimal principal = loanAccount.getLoanAmount();
        BigDecimal annualRate = loanAccount.getInterestRate();
        int tenure = loanAccount.getTenureMonths();

        if (principal == null || principal.signum() <= 0
                || annualRate == null || annualRate.signum() < 0
                || tenure <= 0) {
            throw new IllegalArgumentException(
                    "Invalid loan principal, interest rate or tenure");
        }

        LocalDate disbursementDate =
                loanAccount.getDisbursementDate().toLocalDate();

        int emiDay = loanAccount.getLoanDeal().getEmiDay();

        LocalDate firstEmiDate =
                generateFirstEmiDate(disbursementDate, emiDay);

        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(1200), 12,
                        RoundingMode.HALF_UP);

        BigDecimal emi = calculateEmi(principal, monthlyRate, tenure);

        List<EmiSchedule> schedules = new ArrayList<>();
        BigDecimal openingBalance = principal;

        for (int i = 1; i <= tenure; i++) {

            BigDecimal interestAmount = openingBalance
                    .multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal principalAmount =
                    emi.subtract(interestAmount)
                            .setScale(2, RoundingMode.HALF_UP);

            BigDecimal actualEmi = emi;

            // Adjust the final installment to clear rounding differences.
            if (i == tenure || principalAmount.compareTo(openingBalance) >= 0) {
                principalAmount = openingBalance;
                actualEmi = principalAmount.add(interestAmount)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal closingBalance = openingBalance
                    .subtract(principalAmount)
                    .max(BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);

            EmiSchedule schedule = new EmiSchedule();
            schedule.setLoanAccount(loanAccount);
            schedule.setInstallmentNo(i);
            schedule.setDueDate(firstEmiDate);
            schedule.setOpeningBalance(openingBalance);
            schedule.setInterestAmount(interestAmount);
            schedule.setPrincipalAmount(principalAmount);
            schedule.setClosingBalance(closingBalance);
            schedule.setEmi(actualEmi);
            schedule.setPaymentStatus("PENDING");

            schedules.add(schedule);

            openingBalance = closingBalance;
            firstEmiDate = firstEmiDate.plusMonths(1);
        }

        return emiSchedulerRepo.saveAll(schedules)
                .stream()
                .map(schedule -> modelMapper.map(
                        schedule, EmiSchedulerResponseDto.class))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------
    // 2. REGENERATE FUTURE EMIs AFTER SUCCESSFUL PART-PAYMENT
    // ---------------------------------------------------------


    @Override
    @Transactional
    public List<EmiSchedulerResponseDto>
    regenerateFutureEmisAfterPartPayment(int loanAccountId) {

        LoanAccount loanAccount = loanAccountRepo.findById(loanAccountId)
                .orElseThrow(() ->
                        new RuntimeException("Loan account not found"));

        BigDecimal principal = loanAccount.getOutstandingPrincipal();
        BigDecimal annualRate = loanAccount.getInterestRate();

        if (principal == null || principal.signum() < 0
                || annualRate == null || annualRate.signum() < 0) {
            throw new IllegalArgumentException(
                    "Invalid outstanding principal or interest rate");
        }

        if (principal.signum() == 0) {
            throw new IllegalStateException(
                    "Outstanding principal is zero; settle the loan instead");
        }

        LocalDate today = LocalDate.now();

        List<EmiSchedule> allSchedules =
                emiSchedulerRepo.findByLoanAccountLoanAccountId(loanAccountId);

        // Only future installments that can be rescheduled.
        List<EmiSchedule> futureUnpaid = allSchedules.stream()
                .filter(e -> e.getDueDate() != null
                        && e.getDueDate().isAfter(today))
                .filter(e -> "PENDING".equalsIgnoreCase(e.getPaymentStatus())
                        || "UNPAID".equalsIgnoreCase(e.getPaymentStatus()))
                .sorted(
                        Comparator.comparing(EmiSchedule::getDueDate)
                                .thenComparing(EmiSchedule::getInstallmentNo)
                )
                .toList();

        if (futureUnpaid.isEmpty()) {
            throw new IllegalStateException(
                    "No future unpaid installments are available to reschedule");
        }

        /*
         * Use the remaining scheduled installments, not the original
         * total tenure and not the outstanding principal as an EMI.
         *
         * If the schedule has already been shortened or is incomplete,
         * stop instead of accidentally calculating a one-installment EMI.
         */
        int maxInstallmentNo = allSchedules.stream()
                .map(EmiSchedule::getInstallmentNo)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElseThrow(() ->
                        new IllegalStateException("No installment numbers found"));

        long remainingScheduledCount = allSchedules.stream()
                .filter(e -> e.getDueDate() != null
                        && e.getDueDate().isAfter(today))
                .filter(e -> "PENDING".equalsIgnoreCase(e.getPaymentStatus())
                        || "UNPAID".equalsIgnoreCase(e.getPaymentStatus()))
                .count();

        if (remainingScheduledCount != futureUnpaid.size()) {
            throw new IllegalStateException(
                    "EMI schedule contains inconsistent future installments");
        }

        // Do not proceed when only one future EMI remains unexpectedly.
        if (futureUnpaid.size() == 1 && maxInstallmentNo > 1) {
            throw new IllegalStateException(
                    "Only one future unpaid EMI exists. Verify the original "
                            + "schedule before regenerating after part-payment.");
        }

        int remainingInstallments = futureUnpaid.size();

        BigDecimal monthlyRate = annualRate.divide(
                BigDecimal.valueOf(1200),
                12,
                RoundingMode.HALF_UP
        );

        BigDecimal newEmi =
                calculateEmi(principal, monthlyRate, remainingInstallments);

        /*
         * Preserve the original rows for audit/history.
         * Only future unpaid rows are cancelled.
         */
        emiSchedulerRepo.cancelFutureUnpaidEmis(
                loanAccountId,
                today,
                "Rescheduled after part-payment"
        );

        List<EmiSchedule> replacementSchedules = new ArrayList<>();
        BigDecimal openingBalance = principal;

        for (int i = 0; i < futureUnpaid.size(); i++) {

            EmiSchedule oldSchedule = futureUnpaid.get(i);

            BigDecimal interestAmount = openingBalance
                    .multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal principalAmount = newEmi
                    .subtract(interestAmount)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal actualEmi = newEmi;

            // The final installment clears the remaining principal.
            if (i == futureUnpaid.size() - 1
                    || principalAmount.compareTo(openingBalance) >= 0) {

                principalAmount = openingBalance;

                actualEmi = principalAmount
                        .add(interestAmount)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal closingBalance = openingBalance
                    .subtract(principalAmount)
                    .max(BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);

            EmiSchedule replacement = new EmiSchedule();

            replacement.setLoanAccount(loanAccount);
            replacement.setInstallmentNo(oldSchedule.getInstallmentNo());
            replacement.setDueDate(oldSchedule.getDueDate());
            replacement.setOpeningBalance(
                    openingBalance.setScale(2, RoundingMode.HALF_UP));
            replacement.setInterestAmount(interestAmount);
            replacement.setPrincipalAmount(principalAmount);
            replacement.setClosingBalance(closingBalance);
            replacement.setEmi(actualEmi);
            replacement.setPaymentStatus("PENDING");

            replacementSchedules.add(replacement);
            openingBalance = closingBalance;
        }

        return emiSchedulerRepo.saveAll(replacementSchedules)
                .stream()
                .map(schedule -> modelMapper.map(
                        schedule,
                        EmiSchedulerResponseDto.class))
                .collect(Collectors.toList());
    }


    // ---------------------------------------------------------
    // 3. GET EMI SCHEDULE WITH PAGINATION
    // ---------------------------------------------------------

    @Override
    public Page<EmiSchedulerResponseDto>
    getEmiScheduleforParticularCustomer(
            int loanId, Pageable pageable) {

        Page<EmiSchedule> emiScheduleList =
                emiSchedulerRepo.findEmiSchedulesByLoanId(
                        loanId, pageable);

        return emiScheduleList.map(
                schedule -> modelMapper.map(
                        schedule, EmiSchedulerResponseDto.class));
    }

    // ---------------------------------------------------------
    // 4. EMI CALCULATION
    // ---------------------------------------------------------

    private BigDecimal calculateEmi(
            BigDecimal principal,
            BigDecimal monthlyRate,
            int tenure) {

        if (tenure <= 0) {
            throw new IllegalArgumentException(
                    "Remaining tenure must be greater than zero");
        }

        if (monthlyRate.signum() == 0) {
            return principal.divide(
                    BigDecimal.valueOf(tenure),
                    2, RoundingMode.HALF_UP);
        }

        double p = principal.doubleValue();
        double r = monthlyRate.doubleValue();
        double power = Math.pow(1 + r, tenure);

        double result = p * r * power / (power - 1);

        if (!Double.isFinite(result)) {
            throw new IllegalArgumentException(
                    "Unable to calculate EMI");
        }

        return BigDecimal.valueOf(result)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // ---------------------------------------------------------
    // 5. FIRST EMI DATE
    // ---------------------------------------------------------

    private LocalDate generateFirstEmiDate(
            LocalDate disbursementDate, int emiDay) {

        if (emiDay < 1 || emiDay > 28) {
            throw new IllegalArgumentException(
                    "EMI day must be between 1 and 28");
        }

        return disbursementDate.plusMonths(1)
                .withDayOfMonth(emiDay);
    }
}
