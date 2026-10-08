package com.loanapplication.cibil;

import com.loanapplication.dto.CibilScoreRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;

@Component
public class CibilScoreCalculator {

    public ScoreBreakdown calculate(CibilScoreRequest request) {
        int incomeScore = calculateIncomeScore(request.getMonthlyIncome());
        int employmentScore = calculateEmploymentScore(request.getEmploymentType());
        int age = calculateAge(request.getDob());
        int ageScore = calculateAgeScore(age);
        double foir = calculateFoir(request.getMonthlyExpenses(), request.getMonthlyIncome());
        int foirScore = calculateFoirScore(foir);

        int totalScore = incomeScore + employmentScore + ageScore + foirScore;

        return new ScoreBreakdown(
                totalScore,
                incomeScore,
                employmentScore,
                ageScore,
                foirScore,
                foir
        );
    }

    private int calculateIncomeScore(BigDecimal income) {
        if (income.compareTo(new BigDecimal("25000")) < 0) {
            return 100;
        }
        if (income.compareTo(new BigDecimal("50000")) < 0) {
            return 200;
        }
        if (income.compareTo(new BigDecimal("100000")) <= 0) {
            return 300;
        }
        return 400;
    }

    private int calculateEmploymentScore(String employmentType) {
        return switch (employmentType.trim().toLowerCase(Locale.ROOT)) {
            case "government", "govt", "government employee" -> 200;
            case "private", "private employee" -> 150;
            case "self employed", "self-employed", "selfemployed" -> 100;
            default -> throw new IllegalArgumentException(
                    "Unsupported employment type: " + employmentType);
        };
    }

    private int calculateAge(LocalDate dob) {
        int age = Period.between(dob, LocalDate.now()).getYears();
        if (age < 21 || age > 60) {
            throw new IllegalArgumentException(
                    "Customer age must be between 21 and 60 years");
        }
        return age;
    }

    private int calculateAgeScore(int age) {
        if (age <= 24) {
            return 50;
        }
        if (age <= 45) {
            return 150;
        }
        return 100;
    }

    private double calculateFoir(BigDecimal monthlyExpenses, BigDecimal monthlyIncome) {
        return monthlyExpenses
                .divide(monthlyIncome, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    private int calculateFoirScore(double foir) {
        if (foir < 30) {
            return 250;
        }
        if (foir <= 50) {
            return 150;
        }
        if (foir <= 60) {
            return 75;
        }
        return 0;
    }

    public String getStatus(int score) {
        if (score >= 900) {
            return "EXCELLENT";
        }
        if (score >= 800) {
            return "VERY GOOD";
        }
        if (score >= 750) {
            return "GOOD";
        }
        if (score >= 700) {
            return "AVERAGE";
        }
        if (score >= 650) {
            return "RISKY";
        }
        return "BELOW RISKY THRESHOLD";
    }

    public record ScoreBreakdown(
            int totalScore,
            int incomeScore,
            int employmentScore,
            int ageScore,
            int foirScore,
            double foirPercentage
    ) {}
}
