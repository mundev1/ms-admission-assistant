package com.admissions.engine.weighting;

import com.admissions.engine.model.NormalizedProfile;
import com.admissions.engine.model.ScoringWeights;
import com.admissions.engine.normalizer.ScaleNormalizer;

/**
 * European regional weighting matrix.
 *
 * <p>Formula:</p>
 * <pre>
 *   score = w_gpa   · normGpa
 *         + w_ects  · normEcts
 *         + w_lang  · normLanguage
 *         + w_thesis· normThesis
 *         + w_cw    · normCoursework
 * </pre>
 *
 * <p>European programs emphasise ECTS credit coverage (structured CS curriculum depth)
 * and thesis completion over standardized test scores like the GRE.</p>
 *
 * <p>The result is in [0, 1] and is scaled to [0, 100] by the base class
 * {@link RegionalWeightingMatrix#calculateScore}.</p>
 */
public class EuropeWeightingMatrix extends RegionalWeightingMatrix {

    /**
     * Constructs a Europe weighting matrix with the provided normalizer.
     *
     * @param normalizer the score normalizer; must not be {@code null}
     */
    public EuropeWeightingMatrix(ScaleNormalizer normalizer) {
        super(normalizer);
    }

    /**
     * Applies the Europe-specific weighted scoring formula.
     *
     * <p>GRE scores are deliberately excluded from the European formula, as most
     * European universities do not require them.</p>
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
                     + safeWeight(weights.getWeightEcts())       * profile.normalizedEcts()
                     + safeWeight(weights.getWeightLanguage())   * profile.normalizedLanguage()
                     + safeWeight(weights.getWeightThesis())     * profile.normalizedThesis()
                     + safeWeight(weights.getWeightCoursework()) * profile.normalizedCoursework();

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
