package com.admissions.engine.controller;

import com.admissions.engine.model.ApplicantProfile;
import com.admissions.engine.model.scholarship.*;
import com.admissions.engine.service.ScholarshipRadarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Global Scholarship & Assistantship Radar.
 * Auto-matches student profiles with major scholarships in Germany, Japan, US/UK, and Europe.
 *
 * <p>Base path: {@code /api/scholarships}</p>
 */
@RestController
@RequestMapping("/api/scholarships")
@CrossOrigin(origins = "*")
public class ScholarshipRadarController {

    private final ScholarshipRadarService radarService;

    public ScholarshipRadarController(ScholarshipRadarService radarService) {
        this.radarService = radarService;
    }

    /**
     * Executes the comprehensive scholarship radar scan against the student's profile.
     *
     * @param request target profile, region, and funding preference filters
     * @return radar results including match scores, categorized awards, and assistantship analysis
     */
    @PostMapping("/radar")
    public ResponseEntity<ScholarshipRadarResult> scanScholarshipRadar(@RequestBody ScholarshipRadarRequest request) {
        ScholarshipRadarResult result = radarService.scanRadar(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Helper endpoint accepting an ApplicantProfile directly for instant radar scanning.
     *
     * @param profile student academic and test profile
     * @return scholarship radar scan result across all global regions
     */
    @PostMapping("/match")
    public ResponseEntity<ScholarshipRadarResult> matchDirectProfile(@RequestBody ApplicantProfile profile) {
        ScholarshipRadarRequest request = new ScholarshipRadarRequest(profile, "ALL", "ALL");
        ScholarshipRadarResult result = radarService.scanRadar(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Evaluates university Graduate Teaching / Research Assistantship (TA/RA) fee remission
     * and monthly stipend potential.
     *
     * @param profile student academic profile
     * @return assistantship fee remission evaluation
     */
    @PostMapping("/assistantship")
    public ResponseEntity<AssistantshipEvaluation> evaluateAssistantship(@RequestBody ApplicantProfile profile) {
        AssistantshipEvaluation eval = radarService.evaluateAssistantship(profile);
        return ResponseEntity.ok(eval);
    }

    /**
     * Retrieves the curated database of all global scholarship opportunities.
     *
     * @param region       optional region filter ("GERMANY", "JAPAN", "US_UK", "EUROPE")
     * @param coverageType optional coverage type ("FULL_RIDE", "TUITION_PLUS_STIPEND", etc.)
     * @return list of matching scholarship opportunities
     */
    @GetMapping("/opportunities")
    public ResponseEntity<List<ScholarshipOpportunity>> getOpportunities(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String coverageType) {

        List<ScholarshipOpportunity> list = radarService.getAllOpportunities();

        if (region != null && !region.isBlank() && !"ALL".equalsIgnoreCase(region)) {
            list = list.stream()
                    .filter(s -> s.getRegion().equalsIgnoreCase(region.trim()))
                    .collect(Collectors.toList());
        }

        if (coverageType != null && !coverageType.isBlank() && !"ALL".equalsIgnoreCase(coverageType)) {
            list = list.stream()
                    .filter(s -> s.getCoverageType().equalsIgnoreCase(coverageType.trim()))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(list);
    }

    /**
     * Retrieves detailed information for a specific scholarship opportunity.
     *
     * @param id scholarship identifier (e.g. "DAAD_EPOS", "MEXT_EMBASSY")
     * @return scholarship opportunity details or 404 Not Found
     */
    @GetMapping("/opportunities/{id}")
    public ResponseEntity<ScholarshipOpportunity> getOpportunityById(@PathVariable String id) {
        return radarService.getOpportunityById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
