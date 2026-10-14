
package com.loanapplication.service.impl;

import com.loanapplication.dto.CibilRequestDto;
import com.loanapplication.dto.CibilResponseDto;
import com.loanapplication.entities.CibilReport;
import com.loanapplication.entities.Customer;
import com.loanapplication.repo.CibilReportRepo;
import com.loanapplication.repo.CustomerRepo;
import com.loanapplication.service.CibilService;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class CibilServiceImpl implements CibilService {

    private final CibilReportRepo cibilReportRepo;
    private final CustomerRepo customerRepo;
    private final ModelMapper modelMapper;

    public CibilServiceImpl(
            CibilReportRepo cibilReportRepo,
            CustomerRepo customerRepo,
            ModelMapper modelMapper) {

        this.cibilReportRepo = cibilReportRepo;
        this.customerRepo = customerRepo;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public CibilResponseDto generateCibilScore(CibilRequestDto request) {

        validateRequest(request);

        Customer customer = customerRepo.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Customer not found: " + request.getCustomerId()));

        int incomeScore = calculateIncomeScore(
                request.getGrossMonthlySalary());

        int employmentScore = calculateEmploymentScore(
                request.getEmploymentType());

        int ageScore = calculateAgeScore(request.getAge());

        BigDecimal foir = request.getTotalMonthlyDebtPayment()
                .multiply(BigDecimal.valueOf(100))
                .divide(request.getGrossMonthlySalary(),
                        2, RoundingMode.HALF_UP);

        int foirScore = calculateFoirScore(foir);

        int totalScore = incomeScore + employmentScore
                + ageScore + foirScore;


        CibilReport report = new CibilReport();

        report.setCustomer(customer);
        report.setPanNo(request.getPanNo()
                .trim().toUpperCase(Locale.ROOT));

        report.setIncomeScore(incomeScore);
        report.setEmploymentScore(employmentScore);
        report.setAgeScore(ageScore);
        report.setFoirScore(foirScore);
        report.setFoirPercentage(foir);

        report.setCibilScore(totalScore);
        report.setCheckDate(LocalDateTime.now());

        CibilReport saved = cibilReportRepo.save(report);

        return toResponse(saved);

    }

    @Override
    @Transactional(readOnly = true)
    public CibilResponseDto getLatestCibilReport(Integer customerId) {

        CibilReport report = cibilReportRepo
                .findTopByCustomerCustomerIdOrderByCheckDateDesc(customerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "CIBIL report not found for customer: " + customerId));

        return toResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public CibilResponseDto getCibilReportById(Integer reportId) {

        CibilReport report = cibilReportRepo.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "CIBIL report not found: " + reportId));

        return toResponse(report);
    }

    private void validateRequest(CibilRequestDto request) {

        if (request == null ||
                request.getCustomerId() == null ||
                request.getPanNo() == null ||
                request.getGrossMonthlySalary() == null ||
                request.getEmploymentType() == null ||
                request.getAge() == null ||
                request.getTotalMonthlyDebtPayment() == null) {

            throw new IllegalArgumentException(
                    "All CIBIL fields are required");
        }

        if (!request.getPanNo().trim()
                .matches("(?i)[A-Z]{5}[0-9]{4}[A-Z]")) {
            throw new IllegalArgumentException("Invalid PAN format");
        }

        if (request.getGrossMonthlySalary().signum() <= 0 ||
                request.getTotalMonthlyDebtPayment().signum() < 0) {
            throw new IllegalArgumentException(
                    "Salary must be positive and debt cannot be negative");
        }

        if (request.getAge() < 21 || request.getAge() > 60) {
            throw new IllegalArgumentException(
                    "Age must be between 21 and 60");
        }

        if (request.getEmploymentType().isBlank()) {
            throw new IllegalArgumentException(
                    "Employment type is required");
        }
    }

    // Factor 1: Income - Maximum 400
    private int calculateIncomeScore(BigDecimal salary) {

        if (salary.compareTo(BigDecimal.valueOf(25000)) < 0) return 100;
        if (salary.compareTo(BigDecimal.valueOf(50000)) < 0) return 200;
        if (salary.compareTo(BigDecimal.valueOf(100000)) < 0) return 300;
        return 400;
    }

    // Factor 2: Employment - Maximum 200
    private int calculateEmploymentScore(String employmentType) {

        String type = employmentType.trim()
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_');

        return switch (type) {
            case "GOVERNMENT", "GOVERNMENT_EMPLOYEE" -> 200;
            case "PRIVATE", "PRIVATE_EMPLOYEE" -> 150;
            case "SELF_EMPLOYED" -> 100;
            default -> throw new IllegalArgumentException(
                    "Invalid employment type: " + employmentType);
        };
    }

    // Factor 3: Age - Maximum 150
    private int calculateAgeScore(int age) {

        if (age <= 24) return 50;
        if (age <= 45) return 150;
        return 100;
    }

    // Factor 4: FOIR - Maximum 250
    private int calculateFoirScore(BigDecimal foir) {

        if (foir.compareTo(BigDecimal.valueOf(30)) < 0) return 250;
        if (foir.compareTo(BigDecimal.valueOf(50)) <= 0) return 150;
        if (foir.compareTo(BigDecimal.valueOf(60)) <= 0) return 75;
        return 0;
    }

    private CibilResponseDto toResponse(CibilReport report) {

        CibilResponseDto response =
                modelMapper.map(report, CibilResponseDto.class);

        response.setCustomerId(report.getCustomer().getCustomerId());

        int score = report.getCibilScore();

        String status = score >= 900 ? "EXCELLENT" :
                score >= 800 ? "VERY_GOOD" :
                score >= 750 ? "GOOD" :
                score >= 700 ? "AVERAGE" :
                score >= 650 ? "RISKY" : "HIGH_RISK";

        String recommendation = score >= 800
                ? "AUTO_APPROVAL_ELIGIBLE"
                : score >= 650
                  ? "CREDIT_OFFICER_REVIEW" : "REJECT";

        response.setCibilStatus(status);
        response.setApprovalRecommendation(recommendation);
        response.setMessage("Internal four-factor credit score report");

        return response;
    }
}
