package com.admissions.engine.service;

import com.admissions.engine.model.ects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EctsPreScreenerService Equivalence & Prerequisite Tests")
class EctsPreScreenerServiceTest {

    private EctsPreScreenerService ectsService;

    @BeforeEach
    void setUp() {
        ectsService = new EctsPreScreenerService();
    }

    @Test
    @DisplayName("getUniversityPresets returns top German public universities with strict ECTS thresholds")
    void testGetUniversityPresets() {
        List<UniversityEctsRequirement> presets = ectsService.getUniversityPresets();
        assertNotNull(presets);
        assertTrue(presets.size() >= 5);

        UniversityEctsRequirement tum = presets.stream()
                .filter(p -> "TUM".equals(p.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(tum, "TUM preset must exist");
        assertEquals(28.0, tum.getMinTheoreticalCsEcts(), "TUM requires strictly 28 ECTS in Theoretical CS");
        assertEquals(18.0, tum.getMinMathematicsEcts(), "TUM requires 18 ECTS in Mathematics");
    }

    @Test
    @DisplayName("getSampleCurricula returns AICTE Model Curriculum and Tier 2/3 profiles")
    void testGetSampleCurricula() {
        List<CurriculumSample> samples = ectsService.getSampleCurricula();
        assertNotNull(samples);
        assertTrue(samples.size() >= 2);

        boolean hasAicte = samples.stream().anyMatch(s -> "AICTE_BTECH_CSE_MODEL".equals(s.getId()));
        boolean hasTier2 = samples.stream().anyMatch(s -> "TIER_2_3_INDIAN_CSE".equals(s.getId()));

        assertTrue(hasAicte, "AICTE model curriculum must be available");
        assertTrue(hasTier2, "Tier 2/3 curriculum must be available");
    }

    @Test
    @DisplayName("AICTE B.Tech CSE model curriculum meets TUM Informatics prerequisite regulations")
    void testAicteCurriculumAgainstTum() {
        CurriculumSample aicteSample = ectsService.getSampleCurricula().stream()
                .filter(s -> "AICTE_BTECH_CSE_MODEL".equals(s.getId()))
                .findFirst()
                .orElseThrow();

        EctsEvaluationRequest req = new EctsEvaluationRequest();
        req.setTargetUniversityId("TUM");
        req.setConversionMode("STANDARD_AICTE");
        req.setSubjects(aicteSample.getSubjects());

        EctsEvaluationResult result = ectsService.evaluateCurriculum(req);

        assertNotNull(result);
        assertEquals("ADMISSION_ELIGIBLE", result.getOverallVerdict());
        assertTrue(result.getTotalEarnedEcts() >= 110.0);
        assertEquals(0.0, result.getCategoryAnalyses().get(EctsCategory.THEORETICAL_CS).getDeficitEcts());
        assertEquals(0.0, result.getCategoryAnalyses().get(EctsCategory.MATHEMATICS).getDeficitEcts());
        assertNotNull(result.getCurriculumTableMarkdown());
    }

    @Test
    @DisplayName("Curriculum lacking Theoretical CS flags critical deficit and recommends bridge courses")
    void testTheoreticalCsDeficitFlagging() {
        // Create an Indian student curriculum with only 1 Data Structures course and no Theory of Computation
        List<SubjectEntry> deficientSubjects = List.of(
            new SubjectEntry("S1", "Engineering Mathematics I", EctsCategory.MATHEMATICS, 4.0, 3, 1, 0, "A", 5.6, 1),
            new SubjectEntry("S2", "Engineering Mathematics II", EctsCategory.MATHEMATICS, 4.0, 3, 1, 0, "A", 5.6, 2),
            new SubjectEntry("S3", "Discrete Mathematics", EctsCategory.MATHEMATICS, 4.0, 3, 1, 0, "A", 5.6, 3),
            new SubjectEntry("S4", "Data Structures with C++", EctsCategory.THEORETICAL_CS, 4.0, 3, 0, 2, "A", 5.6, 3), // Only 5.6 ECTS
            new SubjectEntry("S5", "Operating Systems", EctsCategory.PRACTICAL_SYSTEMS_CS, 4.0, 3, 0, 2, "A", 5.6, 4),
            new SubjectEntry("S6", "Computer Organization", EctsCategory.PRACTICAL_SYSTEMS_CS, 4.0, 3, 0, 2, "A", 5.6, 4),
            new SubjectEntry("S7", "Computer Networks", EctsCategory.PRACTICAL_SYSTEMS_CS, 4.0, 3, 0, 2, "A", 5.6, 5),
            new SubjectEntry("S8", "DBMS", EctsCategory.PRACTICAL_SYSTEMS_CS, 4.0, 3, 0, 2, "A", 5.6, 5),
            new SubjectEntry("S9", "OOP in Java", EctsCategory.SOFTWARE_ENGINEERING, 4.0, 3, 0, 2, "A", 5.6, 6)
        );

        EctsEvaluationRequest req = new EctsEvaluationRequest();
        req.setTargetUniversityId("TUM"); // requires 28 ECTS Theoretical CS
        req.setConversionMode("STANDARD_AICTE");
        req.setSubjects(deficientSubjects);

        EctsEvaluationResult result = ectsService.evaluateCurriculum(req);

        assertNotNull(result);
        assertEquals("HIGH_REJECTION_RISK", result.getOverallVerdict(), "Shortfall of > 22 ECTS in Theoretical CS must trigger high rejection risk");

        CategoryCreditAnalysis theoAnalysis = result.getCategoryAnalyses().get(EctsCategory.THEORETICAL_CS);
        assertTrue(theoAnalysis.getDeficitEcts() > 20.0, "Theoretical CS deficit must be > 20 ECTS");
        assertEquals("CRITICAL_DEFICIT", theoAnalysis.getStatus());

        assertFalse(result.getRecommendedBridgeCourses().isEmpty(), "Must recommend bridge courses");
        boolean recommendsMitOcw = result.getRecommendedBridgeCourses().stream()
                .anyMatch(b -> b.getTitle().contains("Automata") || b.getTitle().contains("Theory of Computation"));
        assertTrue(recommendsMitOcw, "Must recommend Automata / Theory of Computation bridge courses");
    }

    @Test
    @DisplayName("Custom prerequisite regulations are respected by the evaluator")
    void testCustomRequirementEvaluation() {
        UniversityEctsRequirement custom = new UniversityEctsRequirement();
        custom.setId("CUSTOM_UNI");
        custom.setUniversityName("University of Bonn");
        custom.setProgramName("MSc Computer Science");
        custom.setMinTheoreticalCsEcts(15.0);
        custom.setMinMathematicsEcts(15.0);
        custom.setMinPracticalSystemsEcts(20.0);
        custom.setMinSoftwareEngEcts(10.0);
        custom.setMaxAllowableDeficitEcts(5.0);

        List<SubjectEntry> subjects = List.of(
            new SubjectEntry("S1", "Discrete Mathematics", EctsCategory.MATHEMATICS, 4.0, 3, 1, 0, "A", 5.6, 1),
            new SubjectEntry("S2", "Linear Algebra", EctsCategory.MATHEMATICS, 4.0, 3, 1, 0, "A", 5.6, 2),
            new SubjectEntry("S3", "Calculus", EctsCategory.MATHEMATICS, 4.0, 3, 1, 0, "A", 5.6, 3), // Math = 16.8 ECTS (met)
            new SubjectEntry("S4", "Algorithms & Data Structures", EctsCategory.THEORETICAL_CS, 4.0, 3, 0, 2, "A", 5.6, 3),
            new SubjectEntry("S5", "Theory of Computation", EctsCategory.THEORETICAL_CS, 4.0, 3, 1, 0, "A", 5.6, 4), // Theo = 11.2 ECTS (deficit 3.8 <= 5.0)
            new SubjectEntry("S6", "Operating Systems", EctsCategory.PRACTICAL_SYSTEMS_CS, 4.0, 3, 0, 2, "A", 5.6, 4),
            new SubjectEntry("S7", "Computer Networks", EctsCategory.PRACTICAL_SYSTEMS_CS, 4.0, 3, 0, 2, "A", 5.6, 5),
            new SubjectEntry("S8", "DBMS", EctsCategory.PRACTICAL_SYSTEMS_CS, 4.0, 3, 0, 2, "A", 5.6, 5),
            new SubjectEntry("S9", "Computer Architecture", EctsCategory.PRACTICAL_SYSTEMS_CS, 4.0, 3, 0, 2, "A", 5.6, 6), // Sys = 22.4 ECTS (met)
            new SubjectEntry("S10", "Software Engineering", EctsCategory.SOFTWARE_ENGINEERING, 4.0, 3, 0, 2, "A", 5.6, 6),
            new SubjectEntry("S11", "OOP in Java", EctsCategory.SOFTWARE_ENGINEERING, 4.0, 3, 0, 2, "A", 5.6, 7) // SE = 11.2 ECTS (met)
        );

        EctsEvaluationRequest req = new EctsEvaluationRequest();
        req.setCustomRequirement(custom);
        req.setConversionMode("STANDARD_AICTE");
        req.setSubjects(subjects);

        EctsEvaluationResult result = ectsService.evaluateCurriculum(req);

        assertNotNull(result);
        assertEquals("CONDITIONAL_ADMISSION_RISK", result.getOverallVerdict());
        assertEquals("University of Bonn", result.getUniversityName());
    }
}
