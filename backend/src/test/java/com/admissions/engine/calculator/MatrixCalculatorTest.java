package com.admissions.engine.calculator;

import com.admissions.engine.model.*;
import com.admissions.engine.normalizer.ScaleNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MatrixCalculator and DeficitAnalyzer Integration Tests")
class MatrixCalculatorTest {

    private ScaleNormalizer normalizer;
    private DeficitAnalyzer deficitAnalyzer;
    private MatrixCalculator calculator;

    @BeforeEach
    void setUp() {
        normalizer = ScaleNormalizer.getInstance();
        deficitAnalyzer = new DeficitAnalyzer();
        calculator = new MatrixCalculator(normalizer, deficitAnalyzer);
    }

    @Test
    @DisplayName("US Program: Strong applicant should receive SAFE tier")
    void testUSProgramStrongApplicant() {
        // High profile: 9.0 CGPA (3.6/4.0), 168 Quant, 162 Verbal, 4.5 AW, 110 TOEFL
        ApplicantProfile applicant = ApplicantProfile.builder()
                .applicantId(1)
                .fullName("Aarav Sharma")
                .cgpa10pt(9.0)
                .cgpaScale(10.0)
                .greQuant(168)
                .greVerbal(162)
                .greAw(4.5)
                .toeflScore(110)
                .thesisCompleted(true)
                .internshipMonths(12)
                .build();

        // US Weights: GPA 30%, GRE-Q 25%, GRE-V 15%, GRE-AW 5%, Lang 10%, CW 10%, Thesis 5%
        ScoringWeights usWeights = ScoringWeights.builder()
                .weightId(1)
                .weightGpa(0.30)
                .weightGreQuant(0.25)
                .weightGreVerbal(0.15)
                .weightGreAw(0.05)
                .weightLanguage(0.10)
                .weightCoursework(0.10)
                .weightThesis(0.05)
                .build();

        UniversityProgram program = UniversityProgram.builder()
                .programId(101)
                .universityName("Stanford University")
                .programName("MS Computer Science")
                .region("US_CANADA")
                .thresholdGpa(3.7)
                .thresholdGreQ(165)
                .thresholdGreV(160)
                .thresholdToefl(100)
                .weights(usWeights)
                .build();

        ScoreResult result = calculator.calculateScore(applicant, program);

        assertNotNull(result);
        assertTrue(result.getCompatibilityScore() >= 80.0, "Score should be >= 80%: " + result.getCompatibilityScore());
        assertEquals("SAFE", result.getCompatibilityTier());
        assertEquals("🟢", result.getTierEmoji());
        assertNotNull(result.getDeficitReport());
    }

    @Test
    @DisplayName("DeficitAnalyzer: Should detect GRE Quant gap for US target")
    void testDeficitAnalyzerGreQuant() {
        ApplicantProfile applicant = ApplicantProfile.builder()
                .applicantId(2)
                .fullName("Priya Patel")
                .cgpa10pt(8.5)
                .greQuant(160) // Threshold is 165
                .greVerbal(160)
                .build();

        UniversityProgram program = UniversityProgram.builder()
                .programId(102)
                .universityName("MIT")
                .region("US_CANADA")
                .thresholdGreQ(165)
                .build();

        DeficitReport report = deficitAnalyzer.analyzeDeficit(applicant, program);

        assertFalse(report.isMeetingAllThresholds());
        assertTrue(report.getFieldDeltas().containsKey("greQuant"));
        assertTrue(report.getFieldDeltas().get("greQuant").contains("5 points"));
    }

    @Test
    @DisplayName("Japan Program: Shift weight to JLPT proficiency")
    void testJapanProgramWeighting() {
        // Applicant with JLPT N1 and research proposal
        ApplicantProfile applicant = ApplicantProfile.builder()
                .applicantId(3)
                .fullName("Rohan Verma")
                .cgpa10pt(8.0)
                .jlptLevel("N1")
                .researchScore(90.0)
                .thesisCompleted(true)
                .build();

        ScoringWeights jpWeights = ScoringWeights.builder()
                .weightId(3)
                .weightGpa(0.20)
                .weightLanguage(0.35)
                .weightResearch(0.30)
                .weightCoursework(0.10)
                .weightThesis(0.05)
                .build();

        UniversityProgram program = UniversityProgram.builder()
                .programId(301)
                .universityName("University of Tokyo")
                .region("JAPAN")
                .thresholdJlpt("N2")
                .thresholdGpa(3.0)
                .weights(jpWeights)
                .build();

        ScoreResult result = calculator.calculateScore(applicant, program);

        assertNotNull(result);
        assertTrue(result.getCompatibilityScore() >= 75.0, "Score should be strong due to N1: " + result.getCompatibilityScore());
        assertTrue(result.getDeficitReport().isMeetingAllThresholds());
    }

    @Test
    @DisplayName("European Program: Deficit detected when CS ECTS credits are missing")
    void testEuropeDeficitEctsCredits() {
        ApplicantProfile applicant = ApplicantProfile.builder()
                .applicantId(4)
                .fullName("Devika Nair")
                .cgpa10pt(8.2)
                .ectsCreditsCs(60) // Target is 90
                .ieltsScore(7.5)
                .thesisCompleted(false)
                .build();

        UniversityProgram program = UniversityProgram.builder()
                .programId(201)
                .universityName("TU Munich")
                .region("EUROPE")
                .thresholdEctsCs(90)
                .thresholdIelts(6.5)
                .thesisRequired(true)
                .build();

        DeficitReport report = deficitAnalyzer.analyzeDeficit(applicant, program);

        assertFalse(report.isMeetingAllThresholds());
        assertTrue(report.getFieldDeltas().containsKey("ectsCreditsCs"));
        assertTrue(report.getFieldDeltas().get("ectsCreditsCs").contains("30 ECTS"));
        assertTrue(report.getFieldDeltas().containsKey("thesisCompleted"));
    }

    @Test
    @DisplayName("Holistic Scoring Integration: Combines academic, financial, and visa scores")
    void testHolisticScoringIntegration() {
        ApplicantProfile applicant = ApplicantProfile.builder()
                .applicantId(5)
                .fullName("Vikram Seth")
                .cgpa10pt(8.8)
                .greQuant(168)
                .greVerbal(160)
                .greAw(4.5)
                .toeflScore(105)
                .annualBudgetUsd(70000.0)
                .liquidSavingsUsd(85000.0)
                .postStudyWorkPriority("HIGH")
                .build();

        ScoringWeights usWeights = ScoringWeights.builder()
                .weightId(1)
                .weightGpa(0.30)
                .weightGreQuant(0.25)
                .weightGreVerbal(0.15)
                .weightGreAw(0.05)
                .weightLanguage(0.10)
                .weightCoursework(0.10)
                .weightThesis(0.05)
                .build();

        UniversityProgram program = UniversityProgram.builder()
                .programId(101)
                .universityName("MIT")
                .country("USA")
                .region("US_CANADA")
                .thresholdGpa(3.8)
                .thresholdGreQ(167)
                .thresholdGreV(163)
                .thresholdToefl(100)
                .tuitionAnnualUsd(60000.0)
                .livingCostAnnualUsd(25000.0)
                .blockedAccountRequirementUsd(85000.0)
                .postStudyVisaMonths(36)
                .weights(usWeights)
                .build();

        ScoreResult result = calculator.calculateScore(applicant, program);

        assertNotNull(result);
        assertTrue(result.getCompatibilityScore() > 0);
        assertTrue(result.getFinancialScore() > 0);
        assertEquals(85.0, result.getVisaScore());
        assertTrue(result.getHolisticScore() > 0);
        assertNotNull(result.getHolisticTier());
    }
}
