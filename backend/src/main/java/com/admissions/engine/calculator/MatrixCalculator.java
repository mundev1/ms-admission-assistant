package com.admissions.engine.calculator;

import com.admissions.engine.model.*;
import com.admissions.engine.normalizer.ScaleNormalizer;
import com.admissions.engine.weighting.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Central scoring orchestrator that routes applicant–program pairs to the
 * appropriate {@link RegionalWeightingMatrix} and assembles a complete {@link ScoreResult}.
 *
 * <p>On construction, a registry of regional matrices is built. The correct matrix
 * is selected based on the program's {@code region} field. The deficit analysis is
 * computed in parallel and attached to the result.</p>
 */
@Component
public class MatrixCalculator implements Scorable {

    private final ScaleNormalizer normalizer;
    private final DeficitAnalyzer deficitAnalyzer;
    private final FinancialVisaEvaluator financialVisaEvaluator;

    /**
     * Region key → concrete matrix implementation registry.
     * Keys match {@link UniversityProgram#getRegion()} values.
     */
    private final Map<String, RegionalWeightingMatrix> matrixRegistry;

    /**
     * Constructs a {@code MatrixCalculator} injecting all evaluators.
     *
     * @param normalizer              the shared score normalizer
     * @param deficitAnalyzer         the deficit analysis component
     * @param financialVisaEvaluator  the financial and visa eligibility evaluator
     */
    @org.springframework.beans.factory.annotation.Autowired
    public MatrixCalculator(ScaleNormalizer normalizer,
                            DeficitAnalyzer deficitAnalyzer,
                            FinancialVisaEvaluator financialVisaEvaluator) {
        this.normalizer              = normalizer;
        this.deficitAnalyzer         = deficitAnalyzer;
        this.financialVisaEvaluator  = financialVisaEvaluator != null ? financialVisaEvaluator : new FinancialVisaEvaluator();

        this.matrixRegistry = Map.of(
                "US_CANADA", new USCanadaWeightingMatrix(normalizer),
                "EUROPE",    new EuropeWeightingMatrix(normalizer),
                "JAPAN",     new JapanWeightingMatrix(normalizer)
        );
    }

    /**
     * Overloaded constructor for tests and backwards compatibility.
     */
    public MatrixCalculator(ScaleNormalizer normalizer, DeficitAnalyzer deficitAnalyzer) {
        this(normalizer, deficitAnalyzer, new FinancialVisaEvaluator());
    }

    /**
     * Calculates a full {@link ScoreResult} for the given applicant–program pair.
     *
     * <p>Pipeline:</p>
     * <ol>
     *   <li>Resolve the regional matrix from the registry</li>
     *   <li>Compute the weighted academic compatibility score via the matrix</li>
     *   <li>Run deficit analysis via {@link DeficitAnalyzer}</li>
     *   <li>Evaluate financial feasibility, scholarships, and post-study work visa outlook</li>
     *   <li>Classify academic and holistic scores into tiers</li>
     *   <li>Assemble and return the final {@link ScoreResult}</li>
     * </ol>
     *
     * @param profile the applicant's academic profile; must not be {@code null}
     * @param program the target program (must have {@code weights} attached); must not be {@code null}
     * @return fully populated {@link ScoreResult}
     * @throws IllegalArgumentException if the program's region is not supported
     */
    @Override
    public ScoreResult calculateScore(ApplicantProfile profile, UniversityProgram program) {
        String region = program.getRegion();
        RegionalWeightingMatrix matrix = matrixRegistry.get(region);

        if (matrix == null) {
            throw new IllegalArgumentException(
                    "Unsupported region: '" + region + "'. Supported: " + matrixRegistry.keySet());
        }

        // Compute base academic score from the regional matrix
        ScoreResult baseResult = matrix.calculateScore(profile, program);

        // Compute deficit report
        DeficitReport deficitReport = deficitAnalyzer.analyzeDeficit(profile, program);

        // Classify academic score into a tier
        String tier = classifyTier(baseResult.getCompatibilityScore());

        // Evaluate financial feasibility, scholarships, and post-study work visa outlook
        FinancialVisaEvaluator.FinancialVisaEvaluation fvEval =
                financialVisaEvaluator.evaluate(profile, program, baseResult.getCompatibilityScore());

        // Assemble the final result
        return ScoreResult.builder()
                .applicantId(profile.getApplicantId())
                .programId(program.getProgramId())
                .universityName(program.getUniversityName())
                .programName(program.getProgramName())
                .country(program.getCountry())
                .region(program.getRegion())
                .compatibilityScore(baseResult.getCompatibilityScore())
                .compatibilityTier(tier)
                .normalizedProfile(baseResult.getNormalizedProfile())
                .deficitReport(deficitReport)
                .financialScore(fvEval.financialScore())
                .visaScore(fvEval.visaScore())
                .holisticScore(fvEval.holisticScore())
                .holisticTier(fvEval.holisticTier())
                .scholarshipEligible(fvEval.scholarshipEligible())
                .matchedScholarships(fvEval.matchedScholarships())
                .financialSummary(fvEval.financialSummary())
                .visaSummary(fvEval.visaSummary())
                .tuitionAnnualUsd(program.getTuitionAnnualUsd())
                .livingCostAnnualUsd(program.getLivingCostAnnualUsd())
                .blockedAccountRequirementUsd(program.getBlockedAccountRequirementUsd())
                .postStudyVisaMonths(program.getPostStudyVisaMonths())
                .visaProgramName(program.getVisaProgramName())
                .calculationTimestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Builds a {@link NormalizedProfile} for an applicant in the context of a program.
     *
     * <p>This delegates to the matrix for the program's region, ensuring consistent
     * normalization logic per region. Used for standalone profile inspection.</p>
     *
     * @param profile the applicant's academic profile
     * @param program the target program providing threshold context
     * @return a fully computed {@link NormalizedProfile}
     */
    public NormalizedProfile buildNormalizedProfile(ApplicantProfile profile, UniversityProgram program) {
        String region = program.getRegion();
        RegionalWeightingMatrix matrix = matrixRegistry.getOrDefault(region,
                matrixRegistry.get("US_CANADA"));
        return matrix.buildNormalizedProfile(profile, program);
    }

    /**
     * Resolves the best available language score for an applicant.
     *
     * <p>Priority: JLPT → IELTS → TOEFL → 0.0 (no score available)</p>
     *
     * @param profile the applicant's profile
     * @return normalized language score in [0, 1]
     */
    public double resolveLanguageScore(ApplicantProfile profile) {
        if (profile.getJlptLevel()  != null) return normalizer.normalizeLanguage(profile.getJlptLevel());
        if (profile.getIeltsScore() != null) return normalizer.normalizeLanguage(profile.getIeltsScore());
        if (profile.getToeflScore() != null) return normalizer.normalizeLanguage(profile.getToeflScore());
        return 0.0;
    }

    /**
     * Classifies a compatibility score into a named tier.
     *
     * <table border="1">
     *   <tr><th>Score Range</th><th>Tier</th><th>Interpretation</th></tr>
     *   <tr><td>80–100</td><td>SAFE</td><td>Strong fit, high admission probability</td></tr>
     *   <tr><td>60–79</td><td>MATCH</td><td>Good fit, competitive chance</td></tr>
     *   <tr><td>40–59</td><td>REACH</td><td>Partial fit, admission uncertain</td></tr>
     *   <tr><td>0–39</td><td>UNLIKELY</td><td>Significant gaps, low probability</td></tr>
     * </table>
     *
     * @param score compatibility score in [0, 100]
     * @return one of: {@code "SAFE"}, {@code "MATCH"}, {@code "REACH"}, {@code "UNLIKELY"}
     */
    public static String classifyTier(double score) {
        if (score >= 80.0) return "SAFE";
        if (score >= 60.0) return "MATCH";
        if (score >= 40.0) return "REACH";
        return "UNLIKELY";
    }
}
