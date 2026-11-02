package com.admissions.engine.normalizer;

/**
 * Contract for components that can normalize raw academic scores to a standard domain.
 *
 * <p>Implementors translate heterogeneous credential formats (CGPA on different scales,
 * language tests, etc.) into dimensionless values suitable for weighted scoring.</p>
 */
public interface ScoreNormalizable {

    /**
     * Normalizes a raw GPA from its source scale to the engine's internal [0, 1] domain.
     *
     * @param rawGpa      the applicant's GPA on the source scale
     * @param sourceScale the maximum value of the source scale (e.g. 4.0, 10.0)
     * @return normalized GPA in [0, 1]
     */
    double normalizeGpa(double rawGpa, double sourceScale);

    /**
     * Normalizes a language proficiency score to [0, 1].
     *
     * <p>The {@code score} parameter is typed as {@link Object} to accommodate
     * heterogeneous score types: {@link Integer} (TOEFL), {@link Double} (IELTS),
     * and {@link String} (JLPT level).</p>
     *
     * @param score the raw language score; must be a recognized type
     * @return normalized language score in [0, 1]
     */
    double normalizeLanguage(Object score);

    /**
     * Normalizes an arbitrary raw score to [0, 1] via linear mapping over [min, max].
     *
     * <p>Values outside [min, max] are clamped to the boundary.</p>
     *
     * @param raw the raw score; must be castable to {@link Number}
     * @param min the minimum possible raw value (maps to 0.0)
     * @param max the maximum possible raw value (maps to 1.0)
     * @return normalized score in [0, 1]
     */
    double normalizeToDomain(Object raw, double min, double max);
}
