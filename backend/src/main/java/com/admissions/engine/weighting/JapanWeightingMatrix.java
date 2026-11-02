package com.admissions.engine.weighting;

import com.admissions.engine.model.NormalizedProfile;
import com.admissions.engine.model.ScoringWeights;
import com.admissions.engine.normalizer.ScaleNormalizer;

/**
 * Japan regional weighting matrix.
 *
 * <p>Formula:</p>
 * <pre>
 *   score = w_gpa      · normGpa
 *         + w_lang     · normLanguage      (JLPT is the primary language component, weight ≈ 0.35)
 *         + w_research · normResearch
 *         + w_cw       · normCoursework
 *         + w_thesis   · normThesis
 * </pre>
 *
 * <p>Japanese graduate programs heavily weight Japanese language proficiency (JLPT)
 * and research capability, with GPA serving as a supporting criterion.</p>
 *
 * <p>The result is in [0, 1] and is scaled to [0, 100] by the base class
 * {@link RegionalWeightingMatrix#calculateScore}.</p>
 */
public class JapanWeightingMatrix extends RegionalWeightingMatrix {

    /**
     * Constructs a Japan weighting matrix with the provided normalizer.
     *
     * @param normalizer the score normalizer; must not be {@code null}
     */
    public JapanWeightingMatrix(ScaleNormalizer normalizer) {
        super(normalizer);
    }

    /**
     * Applies the Japan-specific weighted scoring formula.
     *
     * <p>The language component ({@code normLanguage}) is populated from the applicant's
     * JLPT level by the {@link RegionalWeightingMatrix#resolveLanguageScore} helper.
     * GRE scores are not considered in this formula.</p>
     *
     * {@inheritDoc}
     *
     * @param profile normalized applicant scores
     * @param weights active framework weights for this program
     * @return weighted score in [0, 1]; the base class scales it to [0, 100]
     */
    @Override
    protected double computeWeightedScore(NormalizedProfile profile, ScoringWeights weights) {
        double score = safeWeight(weights.getWeightGpa())        * profile.normalizedGpa()
                     + safeWeight(weights.getWeightLanguage())   * profile.normalizedLanguage()  // JLPT
                     + safeWeight(weights.getWeightResearch())   * profile.normalizedResearch()
                     + safeWeight(weights.getWeightCoursework()) * profile.normalizedCoursework()
                     + safeWeight(weights.getWeightThesis())     * profile.normalizedThesis();

        return Math.min(Math.max(score, 0.0), 1.0);
    }

    /**
     * Null-safe weight accessor.
     *
     * @param weight nullable Double weight value
     * @return the weight value, or 0.0 if null
     */
    private double safeWeight(Double weight) {
        return weight != null ? weight : 0.0;
    }
}
