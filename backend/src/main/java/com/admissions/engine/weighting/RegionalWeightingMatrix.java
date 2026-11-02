package com.admissions.engine.weighting;

import com.admissions.engine.calculator.Scorable;
import com.admissions.engine.model.*;
import com.admissions.engine.normalizer.ScaleNormalizer;

/**
 * Abstract base class for regional weighting matrices.
 *
 * <p>Provides the {@link #calculateScore} orchestration method (from {@link Scorable})
 * and the {@link #applyWeights} generic dot-product helper. Concrete subclasses
 * supply the region-specific {@link #computeWeightedScore} formula.</p>
 */
public abstract class RegionalWeightingMatrix implements Scorable {

    /**
     * Shared normalization utility available to all regional subclasses.
     */
    protected final ScaleNormalizer normalizer;

    /**
     * Constructs the matrix with a normalizer dependency.
     *
     * @param normalizer the score normalizer to use; must not be {@code null}
     */
    protected RegionalWeightingMatrix(ScaleNormalizer normalizer) {
        this.normalizer = normalizer;
    }

    /**
     * Computes the region-specific weighted compatibility score.
     *
     * <p>Each subclass implements this method to apply its own formula,
     * combining normalized dimension scores with regional weights.</p>
     *
     * @param profile the normalized applicant profile
     * @param weights the active scoring weights for the program's framework
     * @return raw weighted score (before capping at 100)
     */
    protected abstract double computeWeightedScore(NormalizedProfile profile, ScoringWeights weights);

    /**
     * {@inheritDoc}
     *
     * <p>Orchestrates the full scoring pipeline:</p>
     * <ol>
     *   <li>Asserts that the program has weights attached</li>
     *   <li>Delegates to the abstract {@link #computeWeightedScore}</li>
     *   <li>Scales the result to [0, 100] and caps at 100</li>
     * </ol>
     *
     * <p>Note: {@link NormalizedProfile} construction and {@link com.admissions.engine.model.DeficitReport}
     * generation are handled upstream in {@link com.admissions.engine.calculator.MatrixCalculator}
     * before this method is invoked with a pre-populated {@link ScoreResult} builder chain.
     * This method is called from {@code MatrixCalculator} via the matrix registry.</p>
     *
     * @param profile the applicant's academic profile
     * @param program the target program (must have {@code weights} set)
     * @return a {@link ScoreResult} with a capped compatibility score
     * @throws IllegalStateException if the program has no weights attached
     */
    @Override
    public ScoreResult calculateScore(ApplicantProfile profile, UniversityProgram program) {
        ScoringWeights weights = program.getWeights();
        if (weights == null) {
            throw new IllegalStateException(
                    "Program (id=" + program.getProgramId() + ") has no ScoringWeights attached.");
        }

        NormalizedProfile normalizedProfile = buildNormalizedProfile(profile, program);
        double raw = computeWeightedScore(normalizedProfile, weights);
        double capped = Math.min(raw * 100.0, 100.0);

        return ScoreResult.builder()
                .applicantId(profile.getApplicantId())
                .programId(program.getProgramId())
                .compatibilityScore(Math.round(capped * 100.0) / 100.0)
                .normalizedProfile(normalizedProfile)
                .build();
    }

    /**
     * Generic weighted dot-product helper.
     *
     * <p>Computes:</p>
     * <pre>
     *   score = w_gpa·normGpa + w_greV·normGreVerbal + w_greQ·normGreQuant
     *         + w_greAw·normGreAw + w_lang·normLanguage + w_ects·normEcts
     *         + w_research·normResearch + w_cw·normCoursework + w_thesis·normThesis
     * </pre>
     * <p>Null weights are treated as 0.0.</p>
     *
     * @param p the normalized profile
     * @param w the scoring weights
     * @return the dot product of dimensions × weights
     */
    protected double applyWeights(NormalizedProfile p, ScoringWeights w) {
        return safe(w.getWeightGpa())        * p.normalizedGpa()
             + safe(w.getWeightGreVerbal())  * p.normalizedGreVerbal()
             + safe(w.getWeightGreQuant())   * p.normalizedGreQuant()
             + safe(w.getWeightGreAw())      * p.normalizedGreAw()
             + safe(w.getWeightLanguage())   * p.normalizedLanguage()
             + safe(w.getWeightEcts())       * p.normalizedEcts()
             + safe(w.getWeightResearch())   * p.normalizedResearch()
             + safe(w.getWeightCoursework()) * p.normalizedCoursework()
             + safe(w.getWeightThesis())     * p.normalizedThesis();
    }

    /**
     * Builds a {@link NormalizedProfile} from the applicant's raw profile and the
     * program's threshold context. Subclasses may override for region-specific behaviour.
     *
     * @param profile the raw applicant profile
     * @param program the target university program
     * @return a fully populated {@link NormalizedProfile}
     */
    public NormalizedProfile buildNormalizedProfile(ApplicantProfile profile,
                                                       UniversityProgram program) {
        double normGpa    = normalizer.normalizeToUS4pt(profile.getCgpa10pt()) / 4.0;
        double normGreV   = profile.getGreVerbal()  != null
                ? normalizer.normalizeGreScore(profile.getGreVerbal())  : 0.0;
        double normGreQ   = profile.getGreQuant()   != null
                ? normalizer.normalizeGreScore(profile.getGreQuant())   : 0.0;
        double normGreAw  = profile.getGreAw()      != null
                ? normalizer.normalizeGreAw(profile.getGreAw())         : 0.0;

        double normLang   = resolveLanguageScore(profile);

        double normEcts   = (profile.getEctsCreditsCs() != null
                && program.getThresholdEctsCs() != null
                && program.getThresholdEctsCs() > 0)
                ? normalizer.normalizeEctsCredits(profile.getEctsCreditsCs(),
                                                  program.getThresholdEctsCs())
                : 0.0;

        double normResearch = profile.getResearchScore() != null
                ? normalizer.normalizeToDomain(profile.getResearchScore(), 0.0, 100.0) : 0.0;

        // Coursework proxy: internship months normalized to max 24 months
        double normCw = normalizer.normalizeToDomain(
                profile.getInternshipMonths() != null ? profile.getInternshipMonths() : 0, 0.0, 24.0);

        double normThesis = Boolean.TRUE.equals(profile.getThesisCompleted()) ? 1.0 : 0.0;

        return NormalizedProfile.builder()
                .normalizedGpa(normGpa)
                .normalizedGreVerbal(normGreV)
                .normalizedGreQuant(normGreQ)
                .normalizedGreAw(normGreAw)
                .normalizedLanguage(normLang)
                .normalizedEcts(normEcts)
                .normalizedResearch(normResearch)
                .normalizedCoursework(normCw)
                .normalizedThesis(normThesis)
                .build();
    }

    /**
     * Resolves the best available language proficiency score for the applicant.
     *
     * <p>Priority: JLPT → IELTS → TOEFL → 0.0</p>
     *
     * @param profile the applicant's profile
     * @return normalized language score in [0, 1]
     */
    protected double resolveLanguageScore(ApplicantProfile profile) {
        if (profile.getJlptLevel()  != null) return normalizer.normalizeLanguage(profile.getJlptLevel());
        if (profile.getIeltsScore() != null) return normalizer.normalizeLanguage(profile.getIeltsScore());
        if (profile.getToeflScore() != null) return normalizer.normalizeLanguage(profile.getToeflScore());
        return 0.0;
    }

    /**
     * Null-safe double extraction; returns 0.0 for null weights.
     *
     * @param value nullable Double weight
     * @return the double value or 0.0 if null
     */
    private double safe(Double value) {
        return value != null ? value : 0.0;
    }
}
