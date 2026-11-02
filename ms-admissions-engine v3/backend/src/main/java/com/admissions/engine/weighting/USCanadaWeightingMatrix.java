package com.admissions.engine.weighting;

import com.admissions.engine.model.NormalizedProfile;
import com.admissions.engine.model.ScoringWeights;
import com.admissions.engine.normalizer.ScaleNormalizer;

/**
 * US/Canada regional weighting matrix.
 *
 * <p>Formula (all components must sum to 1.0 via the weights):</p>
 * <pre>
 *   score = w_gpa  · normGpa
 *         + w_greQ · normGreQuant
 *         + w_greV · normGreVerbal
 *         + w_greAw· normGreAw
 *         + w_lang · normLanguage
 *         + w_cw   · normCoursework
 *         + w_thesis· normThesis
 * </pre>
 *
 * <p>The result is in [0, 1] and is scaled to [0, 100] by the base class
 * {@link RegionalWeightingMatrix#calculateScore}.</p>
 */
public class USCanadaWeightingMatrix extends RegionalWeightingMatrix {

    /**
     * Constructs a US/Canada weighting matrix with the provided normalizer.
     *
     * @param normalizer the score normalizer; must not be {@code null}
     */
    public USCanadaWeightingMatrix(ScaleNormalizer normalizer) {
        super(normalizer);
    }

    /**
     * Applies the US/Canada-specific weighted scoring formula.
     *
     * <p>GRE Quantitative carries the highest weight in this region, reflecting the
     * heavy emphasis STEM programmes place on mathematical reasoning.</p>
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
                     + safeWeight(weights.getWeightGreQuant())   * profile.normalizedGreQuant()
                     + safeWeight(weights.getWeightGreVerbal())  * profile.normalizedGreVerbal()
                     + safeWeight(weights.getWeightGreAw())      * profile.normalizedGreAw()
                     + safeWeight(weights.getWeightLanguage())   * profile.normalizedLanguage()
                     + safeWeight(weights.getWeightCoursework()) * profile.normalizedCoursework()
                     + safeWeight(weights.getWeightThesis())     * profile.normalizedThesis();

        // Ensure the score is clamped to [0, 1] before the base class scales it
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
