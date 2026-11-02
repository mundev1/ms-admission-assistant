package com.admissions.engine.calculator;

import com.admissions.engine.model.ApplicantProfile;
import com.admissions.engine.model.UniversityProgram;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FinancialVisaEvaluator Unit Tests")
class FinancialVisaEvaluatorTest {

    private FinancialVisaEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new FinancialVisaEvaluator();
    }

    @Test
    @DisplayName("Self-funded: Budget fully covers tuition and living expenses")
    void testSelfFundedFullCoverage() {
        ApplicantProfile profile = ApplicantProfile.builder()
                .annualBudgetUsd(90000.0)
                .liquidSavingsUsd(95000.0)
                .postStudyWorkPriority("HIGH")
                .build();

        UniversityProgram program = UniversityProgram.builder()
                .universityName("Stanford University")
                .country("USA")
                .region("US_CANADA")
                .tuitionAnnualUsd(62000.0)
                .livingCostAnnualUsd(26000.0) // Total $88,000
                .blockedAccountRequirementUsd(88000.0)
                .postStudyVisaMonths(36)
                .build();

        FinancialVisaEvaluator.FinancialVisaEvaluation eval = evaluator.evaluate(profile, program, 85.0);

        assertNotNull(eval);
        assertTrue(eval.financialScore() >= 80.0, "Financial score should be high when fully self-funded: " + eval.financialScore());
        assertEquals(85.0, eval.visaScore(), "US STEM OPT baseline score should be 85.0");
        assertTrue(eval.financialSummary().contains("fully covers"));
        // Holistic = 0.50*85.0 + 0.30*financialScore + 0.20*85.0
        assertTrue(eval.holisticScore() >= 80.0);
    }

    @Test
    @DisplayName("Germany Sperrkonto: Blocked account evaluation and DAAD matching")
    void testGermanBlockedAccountAndDaad() {
        // High CGPA + thesis -> triggers DAAD matching
        ApplicantProfile profile = ApplicantProfile.builder()
                .cgpa10pt(8.8)
                .thesisCompleted(true)
                .annualBudgetUsd(10000.0) // Under net annual cost
                .liquidSavingsUsd(15000.0) // Exceeds €11,904 Sperrkonto requirement
                .postStudyWorkPriority("HIGH")
                .build();

        UniversityProgram program = UniversityProgram.builder()
                .universityName("TU Munich")
                .country("Germany")
                .region("EUROPE")
                .tuitionAnnualUsd(6500.0)
                .livingCostAnnualUsd(13000.0) // Total $19,500
                .blockedAccountRequirementUsd(13000.0)
                .postStudyVisaMonths(18)
                .build();

        FinancialVisaEvaluator.FinancialVisaEvaluation eval = evaluator.evaluate(profile, program, 80.0);

        assertNotNull(eval);
        assertTrue(eval.scholarshipEligible(), "Should be eligible for DAAD scholarship");
        assertTrue(eval.matchedScholarships().stream().anyMatch(s -> s.contains("DAAD")));
        assertTrue(eval.financialScore() >= 85.0, "DAAD scholarship should provide strong financial score: " + eval.financialScore());
        assertEquals(92.0, eval.visaScore(), "Germany baseline visa score should be 92.0");
    }

    @Test
    @DisplayName("Japan MEXT Scholarship: Full tuition + living stipend trigger")
    void testJapanMextScholarship() {
        ApplicantProfile profile = ApplicantProfile.builder()
                .cgpa10pt(8.5)
                .researchScore(85.0)
                .jlptLevel("N2")
                .annualBudgetUsd(5000.0)
                .liquidSavingsUsd(8000.0)
                .postStudyWorkPriority("HIGH")
                .build();

        UniversityProgram program = UniversityProgram.builder()
                .universityName("University of Tokyo")
                .country("Japan")
                .region("JAPAN")
                .tuitionAnnualUsd(3600.0)
                .livingCostAnnualUsd(12000.0)
                .blockedAccountRequirementUsd(14000.0)
                .postStudyVisaMonths(24)
                .build();

        FinancialVisaEvaluator.FinancialVisaEvaluation eval = evaluator.evaluate(profile, program, 88.0);

        assertTrue(eval.scholarshipEligible());
        assertTrue(eval.matchedScholarships().stream().anyMatch(s -> s.contains("MEXT")));
        assertTrue(eval.financialScore() >= 90.0, "MEXT full-ride match should boost financial score >= 90: " + eval.financialScore());
        assertEquals(82.0, eval.visaScore());
    }

    @Test
    @DisplayName("Post-study work priority NONE: Visa score is not penalized")
    void testVisaPriorityNone() {
        ApplicantProfile profile = ApplicantProfile.builder()
                .annualBudgetUsd(80000.0)
                .liquidSavingsUsd(50000.0)
                .postStudyWorkPriority("NONE") // Student returning to home country
                .build();

        UniversityProgram program = UniversityProgram.builder()
                .universityName("ETH Zurich")
                .country("Switzerland")
                .region("EUROPE")
                .tuitionAnnualUsd(1600.0)
                .livingCostAnnualUsd(24000.0)
                .blockedAccountRequirementUsd(24000.0)
                .postStudyVisaMonths(6)
                .build();

        FinancialVisaEvaluator.FinancialVisaEvaluation eval = evaluator.evaluate(profile, program, 90.0);

        assertEquals(100.0, eval.visaScore(), "Visa score should be 100 when applicant does not prioritize post-study work");
    }

    @Test
    @DisplayName("Canada PGWP: High baseline visa score (95.0)")
    void testCanadaPgwpScore() {
        ApplicantProfile profile = ApplicantProfile.builder()
                .annualBudgetUsd(40000.0)
                .liquidSavingsUsd(20000.0)
                .postStudyWorkPriority("HIGH")
                .build();

        UniversityProgram program = UniversityProgram.builder()
                .universityName("University of Toronto")
                .country("Canada")
                .region("US_CANADA")
                .tuitionAnnualUsd(35000.0)
                .livingCostAnnualUsd(18000.0)
                .blockedAccountRequirementUsd(15500.0)
                .postStudyVisaMonths(36)
                .build();

        FinancialVisaEvaluator.FinancialVisaEvaluation eval = evaluator.evaluate(profile, program, 82.0);

        assertEquals(95.0, eval.visaScore(), "Canada PGWP should yield 95.0 visa score");
    }
}
