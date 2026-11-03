package com.admissions.engine.service;

import com.admissions.engine.model.alignment.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FacultyAlignmentService NLP & Semantic Critic Tests")
class FacultyAlignmentServiceTest {

    private FacultyAlignmentService alignmentService;

    @BeforeEach
    void setUp() {
        alignmentService = new FacultyAlignmentService();
    }

    @Test
    @DisplayName("getAllFaculty contains curated labs across Japan, US, and Europe")
    void testGetAllFaculty() {
        List<FacultyMember> all = alignmentService.getAllFaculty(null, null);
        assertNotNull(all);
        assertTrue(all.size() >= 8, "Must contain at least 8 faculty members");

        boolean hasTodai = all.stream().anyMatch(f -> f.getUniversityName().contains("Tokyo"));
        boolean hasMit = all.stream().anyMatch(f -> f.getUniversityName().equals("MIT"));
        boolean hasTum = all.stream().anyMatch(f -> f.getUniversityName().contains("Munich"));
        boolean hasSpain = all.stream().anyMatch(f -> f.getCountry().equalsIgnoreCase("Spain"));
        boolean hasPortugal = all.stream().anyMatch(f -> f.getCountry().equalsIgnoreCase("Portugal"));

        assertTrue(hasTodai, "Must contain University of Tokyo faculty");
        assertTrue(hasMit, "Must contain MIT faculty");
        assertTrue(hasTum, "Must contain TU Munich faculty");
        assertTrue(hasSpain, "Must contain Spanish faculty (UPC/UPM)");
        assertTrue(hasPortugal, "Must contain Portuguese faculty (IST/Porto)");
    }

    @Test
    @DisplayName("Analyze Computer Vision & 3D Diffusion proposal matches Todai Harada Lab with Naidaku guidance")
    void testComputerVisionJapaneseAlignment() {
        String cvSop = """
            During my undergraduate research in Computer Science, I focused on 3D computer vision and generative diffusion models.
            My primary research proposal aims to develop controllable latent diffusion models for real-time 3D scene reconstruction
            and multi-modal spatial intelligence for mobile robotics. I have conducted experiments using PyTorch, Gaussian Splatting,
            and camera pose estimation on synthetic benchmarks. I aspire to pursue graduate studies to solve perceptual ambiguities
            in dynamic real-world environments.
            """;

        AlignmentAnalysisRequest req = new AlignmentAnalysisRequest();
        req.setSopText(cvSop);
        req.setTargetUniversityName("University of Tokyo");
        req.setTargetCountry("Japan");
        req.setCandidateName("Aarav Sharma");
        req.setUndergradMajor("B.Tech in Computer Science & AI, IIT Delhi");

        AlignmentAnalysisResult result = alignmentService.analyzeAlignment(req);

        assertNotNull(result);
        assertTrue(result.getOverallAlignmentScore() >= 60.0, "Score should be high for matching 3D diffusion proposal");
        assertFalse(result.getTopFacultyMatches().isEmpty());

        FacultyMatch topMatch = result.getTopFacultyMatches().get(0);
        assertEquals("Prof. Tatsuya Harada", topMatch.getFaculty().getName());
        assertEquals("University of Tokyo", topMatch.getFaculty().getUniversityName());
        assertTrue(topMatch.getMatchingKeywords().contains("diffusion") || topMatch.getMatchingKeywords().contains("vision"));

        // Check Naidaku protocol guidance
        assertNotNull(result.getNaidakuGuidance());
        assertTrue(result.getNaidakuGuidance().contains("Naidaku"));

        // Check pre-generated cold email
        ColdEmailDraft email = result.getSampleColdEmail();
        assertNotNull(email);
        assertTrue(email.getBodyText().contains("Sensei"), "Must address Japanese professor as Sensei");
        assertTrue(email.getBodyText().contains("Controllable 3D Generation via Latent Diffusion Models") ||
                   email.getBodyText().contains("Embodied Vision-Language Navigation"));
        assertTrue(email.getCulturalProtocolNotes().contains("Naidaku"));
    }

    @Test
    @DisplayName("Analyze NLP & Transformer Code Generation proposal matches CMU Graham Neubig")
    void testNlpCmuAlignment() {
        String nlpSop = """
            My research focuses on Natural Language Processing, specifically code generation using large language models
            and autonomous software engineering agents. In my bachelor's thesis, I benchmarked transformer reasoning capabilities
            on multi-lingual code translation datasets. I propose to investigate context compression and mechanistic interpretability
            for developer workflows in open-source repositories.
            """;

        AlignmentAnalysisRequest req = new AlignmentAnalysisRequest();
        req.setSopText(nlpSop);
        req.setTargetUniversityName("CMU");
        req.setTargetCountry("USA");
        req.setCandidateName("Priya Patel");

        AlignmentAnalysisResult result = alignmentService.analyzeAlignment(req);

        assertNotNull(result);
        assertTrue(result.getOverallAlignmentScore() >= 55.0);
        assertFalse(result.getTopFacultyMatches().isEmpty());

        FacultyMatch topMatch = result.getTopFacultyMatches().get(0);
        assertTrue(topMatch.getFaculty().getName().contains("Graham Neubig") || topMatch.getFaculty().getLabName().contains("NeuLab"));
        assertTrue(topMatch.getMatchingKeywords().contains("code") || topMatch.getMatchingKeywords().contains("nlp"));

        // Check US cold email format
        ColdEmailDraft email = result.getSampleColdEmail();
        assertNotNull(email);
        assertTrue(email.getSubjectLine().contains("Prospective MS Thesis Student Inquiry"));
        assertTrue(email.getCulturalProtocolNotes().contains("Keep emails concise"));
    }

    @Test
    @DisplayName("Empty SOP text throws IllegalArgumentException")
    void testEmptySopValidation() {
        AlignmentAnalysisRequest req = new AlignmentAnalysisRequest();
        req.setSopText("   ");

        assertThrows(IllegalArgumentException.class, () -> alignmentService.analyzeAlignment(req));
    }
}
