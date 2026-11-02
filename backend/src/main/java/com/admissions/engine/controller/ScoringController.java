package com.admissions.engine.controller;

import com.admissions.engine.model.ApplicantProfile;
import com.admissions.engine.model.ScoreResult;
import com.admissions.engine.service.ScoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing scoring endpoints for the MS Admissions Matrix Engine.
 *
 * <p>Base path: {@code /api/score}</p>
 *
 * <p>Accepts an {@link ApplicantProfile} in the request body and returns
 * {@link ScoreResult}(s) ranked by compatibility score.</p>
 *
 * <p>{@code @CrossOrigin} is enabled for all origins to support frontend development
 * and local SPA integration.</p>
 */
@RestController
@RequestMapping("/api/score")
@CrossOrigin(origins = "*")
public class ScoringController {

    private final ScoringService scoringService;

    /**
     * Constructs the controller with the required scoring service.
     *
     * @param scoringService the service that orchestrates matrix scoring
     */
    public ScoringController(ScoringService scoringService) {
        this.scoringService = scoringService;
    }

    // ── POST /api/score/region/{region} ──────────────────────────────────────

    /**
     * Scores an applicant against all programs in the specified region and returns
     * up to 20 best-match results, sorted by compatibility score descending.
     *
     * @param region  the target region key (e.g. "US_CANADA", "EUROPE", "JAPAN")
     * @param profile the applicant's academic profile (request body)
     * @return {@code 200 OK} with the ranked list of {@link ScoreResult}s
     */
    @PostMapping("/region/{region}")
    public ResponseEntity<List<ScoreResult>> scoreAgainstRegion(
            @PathVariable String region,
            @RequestBody ApplicantProfile profile) {

        List<ScoreResult> results = scoringService.scoreApplicantAgainstAll(profile, region);
        return ResponseEntity.ok(results);
    }

    // ── POST /api/score/program/{programId} ──────────────────────────────────

    /**
     * Scores an applicant against a single specific university program.
     *
     * @param programId the ID of the target university program
     * @param profile   the applicant's academic profile (request body)
     * @return {@code 200 OK} with a single {@link ScoreResult}
     */
    @PostMapping("/program/{programId}")
    public ResponseEntity<ScoreResult> scoreAgainstProgram(
            @PathVariable int programId,
            @RequestBody ApplicantProfile profile) {

        ScoreResult result = scoringService.scoreApplicantAgainstProgram(profile, programId);
        return ResponseEntity.ok(result);
    }
}
