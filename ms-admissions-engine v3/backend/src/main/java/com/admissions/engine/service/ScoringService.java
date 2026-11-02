package com.admissions.engine.service;

import com.admissions.engine.calculator.MatrixCalculator;
import com.admissions.engine.model.ApplicantProfile;
import com.admissions.engine.model.ScoreResult;
import com.admissions.engine.model.ScoringWeights;
import com.admissions.engine.model.UniversityProgram;
import com.admissions.engine.repository.ScoringWeightsRepository;
import com.admissions.engine.repository.UniversityProgramRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Application service that orchestrates multi-program compatibility scoring
 * for an applicant profile.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Fetching programs for a region from the repository</li>
 *   <li>Attaching active scoring weights to each program</li>
 *   <li>Delegating scoring to {@link MatrixCalculator}</li>
 *   <li>Sorting and truncating results for the caller</li>
 * </ul>
 */
@Service
public class ScoringService {

    private static final int MAX_RESULTS = 20;

    private final MatrixCalculator            matrixCalculator;
    private final UniversityProgramRepository programRepository;
    private final ScoringWeightsRepository    weightsRepository;

    /**
     * Constructs a {@code ScoringService} with its required dependencies.
     *
     * @param matrixCalculator  the scoring orchestrator
     * @param programRepository repository for university programs
     * @param weightsRepository repository for scoring weights
     */
    public ScoringService(MatrixCalculator matrixCalculator,
                          UniversityProgramRepository programRepository,
                          ScoringWeightsRepository weightsRepository) {
        this.matrixCalculator  = matrixCalculator;
        this.programRepository = programRepository;
        this.weightsRepository = weightsRepository;
    }

    /**
     * Scores an applicant against all programs in the specified region.
     *
     * <p>For each program:</p>
     * <ol>
     *   <li>The active {@link ScoringWeights} for the program's framework are loaded.</li>
     *   <li>If weights are found, they are attached to the program as a transient field.</li>
     *   <li>The {@link MatrixCalculator} computes the {@link ScoreResult}.</li>
     *   <li>Programs without active weights are silently skipped.</li>
     * </ol>
     *
     * <p>Results are sorted by {@code compatibilityScore} descending and capped at
     * {@value #MAX_RESULTS} entries.</p>
     *
     * @param profile the applicant to score; must not be {@code null}
     * @param region  the target region key (e.g. "US_CANADA", "EUROPE", "JAPAN")
     * @return list of at most {@value #MAX_RESULTS} {@link ScoreResult}s, best matches first
     */
    public List<ScoreResult> scoreApplicantAgainstAll(ApplicantProfile profile, String region) {
        String normalizedRegion = normalizeRegionKey(region);
        List<UniversityProgram> programs = "ALL".equalsIgnoreCase(normalizedRegion)
                ? programRepository.findAll()
                : programRepository.findByRegion(normalizedRegion);

        return programs.stream()
                .filter(program -> {
                    // Attach weights; skip programs with no active weight configuration
                    return weightsRepository
                            .findByFrameworkIdAndIsActiveTrue(program.getFrameworkId())
                            .map(weights -> {
                                program.setWeights(weights);
                                return true;
                            })
                            .orElse(false);
                })
                .map(program -> matrixCalculator.calculateScore(profile, program))
                .sorted(Comparator.comparingDouble(ScoreResult::getCompatibilityScore).reversed())
                .limit(MAX_RESULTS)
                .toList();
    }

    private String normalizeRegionKey(String region) {
        if (region == null || region.isBlank() || "ALL".equalsIgnoreCase(region.trim())) {
            return "ALL";
        }
        return switch (region.trim().toUpperCase()) {
            case "US", "USA", "US_CANADA" -> "US_CANADA";
            case "EU", "EUROPE"           -> "EUROPE";
            case "JP", "JAPAN"            -> "JAPAN";
            default                       -> region.trim().toUpperCase();
        };
    }

    /**
     * Scores an applicant against a single specific program.
     *
     * <p>Loads the program and its active weights, then delegates to {@link MatrixCalculator}.</p>
     *
     * @param profile   the applicant to score; must not be {@code null}
     * @param programId the ID of the target university program
     * @return the {@link ScoreResult} for this specific pairing
     * @throws NoSuchElementException   if the program or its weights are not found
     * @throws IllegalStateException    if the program has no active scoring weights
     */
    public ScoreResult scoreApplicantAgainstProgram(ApplicantProfile profile, int programId) {
        UniversityProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new NoSuchElementException(
                        "University program not found for id: " + programId));

        ScoringWeights weights = weightsRepository
                .findByFrameworkIdAndIsActiveTrue(program.getFrameworkId())
                .orElseThrow(() -> new IllegalStateException(
                        "No active scoring weights found for frameworkId: " + program.getFrameworkId()));

        program.setWeights(weights);
        return matrixCalculator.calculateScore(profile, program);
    }
}
