package com.loanapplication.service.impl;

import com.loanapplication.cibil.CibilScoreCalculator;
import com.loanapplication.dto.CibilScoreRequest;
import com.loanapplication.dto.CibilScoreResponse;
import com.loanapplication.entities.CibilReport;
import com.loanapplication.entities.Customer;
import com.loanapplication.repo.CibilReportRepository;
import com.loanapplication.service.CibilService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CibilServiceImpl implements CibilService {

    private final CibilScoreCalculator calculator;
    private final CibilReportRepository cibilReportRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public CibilScoreResponse generateScore(CibilScoreRequest request) {
        Customer customer = entityManager.getReference(Customer.class, request.getCustomerId());
        CibilScoreCalculator.ScoreBreakdown result = calculator.calculate(request);

        CibilReport report = new CibilReport();
        report.setCustomer(customer);
        report.setPanNo(request.getPanNo());
        report.setCibilScore(result.totalScore());
        report.setCheckDate(LocalDateTime.now());

        cibilReportRepository.save(report);

        return toResponse(request.getCustomerId(), result);
    }

    @Override
    @Transactional(readOnly = true)
    public CibilScoreResponse getCurrentScore(Integer customerId) {
        List<CibilReport> reports = cibilReportRepository.findByCustomerCustomerIdOrderByCheckDateDesc(customerId);
        if (reports.isEmpty()) {
            throw new EntityNotFoundException("CIBIL report not found for customer: " + customerId);
        }
        CibilReport report = reports.get(0);
        return new CibilScoreResponse(
                customerId,
                report.getCibilScore(),
                calculator.getStatus(report.getCibilScore()),
                null,
                null,
                null,
                null,
                null
        );
    }

    private CibilScoreResponse toResponse(
            Integer customerId,
            CibilScoreCalculator.ScoreBreakdown result) {

        return new CibilScoreResponse(
                customerId,
                result.totalScore(),
                calculator.getStatus(result.totalScore()),
                result.incomeScore(),
                result.employmentScore(),
                result.ageScore(),
                result.foirScore(),
                result.foirPercentage()
        );
    }
}
