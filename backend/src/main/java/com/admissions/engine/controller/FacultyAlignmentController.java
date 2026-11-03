package com.admissions.engine.controller;

import com.admissions.engine.model.alignment.*;
import com.admissions.engine.service.FacultyAlignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Faculty & Lab Alignment Critic (NLP / Semantic Matching).
 *
 * <p>Base path: {@code /api/alignment}</p>
 */
@RestController
@RequestMapping("/api/alignment")
@CrossOrigin(origins = "*")
public class FacultyAlignmentController {

    private final FacultyAlignmentService alignmentService;

    public FacultyAlignmentController(FacultyAlignmentService alignmentService) {
        this.alignmentService = alignmentService;
    }

    /**
     * Analyzes an applicant's Statement of Purpose (SOP) or research proposal against faculty labs.
     *
     * @param request the SOP text and target parameters
     * @return semantic alignment score, matching faculty, literature gaps, critique, and cold email
     */
    @PostMapping("/analyze")
    public ResponseEntity<AlignmentAnalysisResult> analyzeAlignment(@RequestBody AlignmentAnalysisRequest request) {
        AlignmentAnalysisResult result = alignmentService.analyzeAlignment(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Retrieves the curated database of faculty members and active laboratory profiles.
     *
     * @param university optional university filter
     * @param country    optional country filter
     * @return list of matching faculty members
     */
    @GetMapping("/faculty")
    public ResponseEntity<List<FacultyMember>> getFaculty(
            @RequestParam(required = false) String university,
            @RequestParam(required = false) String country) {
        return ResponseEntity.ok(alignmentService.getAllFaculty(university, country));
    }

    /**
     * Retrieves detailed information for a single faculty member.
     *
     * @param facultyId unique faculty identifier
     * @return faculty member details
     */
    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<FacultyMember> getFacultyById(@PathVariable String facultyId) {
        return alignmentService.getFacultyById(facultyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Generates a personalized cold email draft to a specific faculty member.
     *
     * @param request contains target faculty ID and applicant details
     * @return formatted cold email draft with cultural protocol notes
     */
    @PostMapping("/generate-email")
    public ResponseEntity<ColdEmailDraft> generateColdEmail(@RequestBody AlignmentAnalysisRequest request) {
        if (request.getTargetFacultyId() == null || request.getTargetFacultyId().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return alignmentService.getFacultyById(request.getTargetFacultyId())
                .map(f -> {
                    String candName = (request.getCandidateName() != null && !request.getCandidateName().isBlank())
                            ? request.getCandidateName() : "Prospective Candidate";
                    String candMajor = (request.getUndergradMajor() != null && !request.getUndergradMajor().isBlank())
                            ? request.getUndergradMajor() : "B.Tech in Computer Science & Engineering";
                    String sop = request.getSopText() != null ? request.getSopText() : "";
                    ColdEmailDraft email = alignmentService.generateColdEmail(f, candName, candMajor, sop, f.getRecentPublications());
                    return ResponseEntity.ok(email);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
