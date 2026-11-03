package com.admissions.engine.service;

import com.admissions.engine.model.ApplicantProfile;
import com.admissions.engine.model.scholarship.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ScholarshipRadarService Global Scholarship & Assistantship Radar Tests")
class ScholarshipRadarServiceTest {

    private ScholarshipRadarService radarService;

    @BeforeEach
    void setUp() {
        radarService = new ScholarshipRadarService();
    }

    @Test
    @DisplayName("Catalog contains all required scholarships for Germany, Japan, US/UK, and Europe")
    void testCatalogCompleteness() {
        List<ScholarshipOpportunity> all = radarService.getAllOpportunities();
        assertNotNull(all);
        assertTrue(all.size() >= 12, "Curated catalog must contain at least 12 scholarships");

        // Germany: DAAD (EPOS, Helmut-Schmidt, Study Scholarship), Deutschlandstipendium
        assertTrue(all.stream().anyMatch(s -> s.getId().equals("DAAD_EPOS")), "Must contain DAAD EPOS");
        assertTrue(all.stream().anyMatch(s -> s.getId().equals("DAAD_HELMUT_SCHMIDT")), "Must contain DAAD Helmut-Schmidt");
        assertTrue(all.stream().anyMatch(s -> s.getId().equals("DAAD_STUDY_SCHOLARSHIP")), "Must contain DAAD Study Scholarship");
        assertTrue(all.stream().anyMatch(s -> s.getId().equals("DEUTSCHLANDSTIPENDIUM")), "Must contain Deutschlandstipendium");

        // Japan: MEXT Embassy & University, JASSO
        assertTrue(all.stream().anyMatch(s -> s.getId().equals("MEXT_EMBASSY")), "Must contain MEXT Embassy Track");
        assertTrue(all.stream().anyMatch(s -> s.getId().equals("MEXT_UNIVERSITY")), "Must contain MEXT University Track");
        assertTrue(all.stream().anyMatch(s -> s.getId().equals("JASSO_HONORS")), "Must contain JASSO Honors Scholarship");

        // US & UK: Fulbright-Nehru, Inlaks Shivdasani, TA/RA Fee Remission
        assertTrue(all.stream().anyMatch(s -> s.getId().equals("FULBRIGHT_NEHRU")), "Must contain Fulbright-Nehru");
        assertTrue(all.stream().anyMatch(s -> s.getId().equals("INLAKS_SHIVDASANI")), "Must contain Inlaks Shivdasani");
        assertTrue(all.stream().anyMatch(s -> s.getId().equals("US_UK_GRADUATE_TARA")), "Must contain Graduate TA/RA");

        // Europe: Erasmus Mundus EMJM
        assertTrue(all.stream().anyMatch(s -> s.getId().equals("ERASMUS_MUNDUS_EMJM")), "Must contain Erasmus Mundus Joint Masters");
    }

    @Test
    @DisplayName("Radar matches DAAD EPOS when applicant has 24+ months work experience and CGPA >= 7.5")
    void testDaadEposMatching() {
        ApplicantProfile profile = new ApplicantProfile();
        profile.setFullName("Rohan Verma");
        profile.setCgpa10pt(8.2);
        profile.setInternshipMonths(28); // 28 months work experience
        profile.setThesisCompleted(true);
        profile.setToeflScore(95);

        ScholarshipRadarRequest request = new ScholarshipRadarRequest(profile, "GERMANY", "ALL");
        ScholarshipRadarResult result = radarService.scanRadar(request);

        assertNotNull(result);
        ScholarshipMatchResult eposMatch = result.getAllMatches().stream()
                .filter(m -> m.getOpportunity().getId().equals("DAAD_EPOS"))
                .findFirst()
                .orElse(null);

        assertNotNull(eposMatch, "DAAD EPOS should be evaluated for Germany");
        assertEquals("DIRECT_MATCH", eposMatch.getEligibilityStatus(), "Should be DIRECT_MATCH with 28 months experience and 8.2 CGPA");
        assertTrue(eposMatch.getMatchScore() >= 85.0);
        assertTrue(eposMatch.getUnmetRequirements().isEmpty());
    }

    @Test
    @DisplayName("Radar flags professional experience shortfall for DAAD EPOS when work months < 24")
    void testDaadEposShortfall() {
        ApplicantProfile freshGrad = new ApplicantProfile();
        freshGrad.setCgpa10pt(8.5);
        freshGrad.setInternshipMonths(6); // Only 6 months internship

        ScholarshipRadarRequest request = new ScholarshipRadarRequest(freshGrad, "GERMANY", "ALL");
        ScholarshipRadarResult result = radarService.scanRadar(request);

        ScholarshipMatchResult eposMatch = result.getAllMatches().stream()
                .filter(m -> m.getOpportunity().getId().equals("DAAD_EPOS"))
                .findFirst()
                .orElse(null);

        assertNotNull(eposMatch);
        assertTrue(eposMatch.getUnmetRequirements().stream().anyMatch(u -> u.contains("shortfall")));
    }

    @Test
    @DisplayName("Radar matches MEXT Embassy & University Recommendation for research-oriented applicant")
    void testMextMatching() {
        ApplicantProfile profile = new ApplicantProfile();
        profile.setFullName("Priya Patel");
        profile.setCgpa10pt(8.8);
        profile.setThesisCompleted(true);
        profile.setPublicationsCount(1);
        profile.setIeltsScore(7.5);
        profile.setJlptLevel("N3");

        ScholarshipRadarRequest request = new ScholarshipRadarRequest(profile, "JAPAN", "ALL");
        ScholarshipRadarResult result = radarService.scanRadar(request);

        assertNotNull(result);
        assertTrue(result.getAllMatches().size() >= 3);

        ScholarshipMatchResult mextEmbassy = result.getAllMatches().stream()
                .filter(m -> m.getOpportunity().getId().equals("MEXT_EMBASSY"))
                .findFirst()
                .orElse(null);

        assertNotNull(mextEmbassy);
        assertEquals("DIRECT_MATCH", mextEmbassy.getEligibilityStatus());
        assertTrue(mextEmbassy.getOpportunity().getAwardSummary().contains("¥144,000"));
    }

    @Test
    @DisplayName("Radar matches Erasmus Mundus Joint Masters (EMJM) full-ride for strong European candidate")
    void testErasmusMundusMatching() {
        ApplicantProfile profile = new ApplicantProfile();
        profile.setCgpa10pt(8.6);
        profile.setThesisCompleted(true);
        profile.setIeltsScore(7.5);

        ScholarshipRadarRequest request = new ScholarshipRadarRequest(profile, "EUROPE", "ALL");
        ScholarshipRadarResult result = radarService.scanRadar(request);

        assertNotNull(result);
        ScholarshipMatchResult emjm = result.getAllMatches().stream()
                .filter(m -> m.getOpportunity().getId().equals("ERASMUS_MUNDUS_EMJM"))
                .findFirst()
                .orElse(null);

        assertNotNull(emjm);
        assertTrue(emjm.getMatchScore() >= 85.0);
        assertTrue(emjm.getOpportunity().getAwardSummary().contains("€1,400/month"));
    }

    @Test
    @DisplayName("Evaluate US & UK Graduate Assistantship (TA/RA) fee remission calculation")
    void testAssistantshipFeeRemissionEvaluation() {
        ApplicantProfile profile = new ApplicantProfile();
        profile.setCgpa10pt(8.4);
        profile.setGreQuant(166);
        profile.setToeflScore(104);
        profile.setThesisCompleted(true);
        profile.setPublicationsCount(1);

        AssistantshipEvaluation eval = radarService.evaluateAssistantship(profile);

        assertNotNull(eval);
        assertTrue(eval.isTaEligible(), "Should be eligible for Teaching Assistantship");
        assertTrue(eval.isRaEligible(), "Should be eligible for Research Assistantship");
        assertTrue(eval.getEstimatedTuitionRemissionUsd() >= 30000.0, "Tuition remission should be $30k+");
        assertTrue(eval.getEstimatedAnnualStipendUsd() >= 20000.0, "Annual stipend should be $20k+");
        assertTrue(eval.getEstimatedTotalPackageUsd() >= 50000.0, "Total package should be $50k+");
        assertEquals(20, eval.getWeeklyWorkHours());
    }
}
