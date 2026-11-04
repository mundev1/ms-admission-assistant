package com.admissions.engine.controller;

import com.admissions.engine.model.ects.*;
import com.admissions.engine.service.EctsPreScreenerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for German ECTS Credit Equivalence & Prerequisite Pre-Screener.
 *
 * <p>Base path: {@code /api/ects}</p>
 */
@RestController
@RequestMapping("/api/ects")
@CrossOrigin(origins = "*")
public class EctsPreScreenerController {

    private final EctsPreScreenerService ectsPreScreenerService;

    public EctsPreScreenerController(EctsPreScreenerService ectsPreScreenerService) {
        this.ectsPreScreenerService = ectsPreScreenerService;
    }

    /**
     * Retrieves all configured German university prerequisite regulation presets.
     */
    @GetMapping("/presets")
    public ResponseEntity<List<UniversityEctsRequirement>> getUniversityPresets() {
        return ResponseEntity.ok(ectsPreScreenerService.getUniversityPresets());
    }

    /**
     * Retrieves standard Indian university undergraduate curricula for 1-click loading.
     */
    @GetMapping("/sample-curricula")
    public ResponseEntity<List<CurriculumSample>> getSampleCurricula() {
        return ResponseEntity.ok(ectsPreScreenerService.getSampleCurricula());
    }

    /**
     * Retrieves catalog of accredited bridge courses and recognized MOOCs.
     */
    @GetMapping("/bridge-courses")
    public ResponseEntity<List<BridgeCourseRecommendation>> getBridgeCatalog() {
        return ResponseEntity.ok(ectsPreScreenerService.getBridgeCatalog());
    }

    /**
     * Evaluates undergraduate subjects against target university prerequisite regulations.
     */
    @PostMapping("/evaluate")
    public ResponseEntity<EctsEvaluationResult> evaluateCurriculum(@RequestBody EctsEvaluationRequest request) {
        EctsEvaluationResult result = ectsPreScreenerService.evaluateCurriculum(request);
        return ResponseEntity.ok(result);
    }
}
