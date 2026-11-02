package com.admissions.engine.normalizer;

import org.springframework.stereotype.Component;

/**
 * Core normalization service that converts heterogeneous academic credentials
 * (GPA on various scales, GRE/GMAT, IELTS, TOEFL, JLPT, ECTS credits) into
 * dimensionless values in the [0, 1] domain.
 *
 * <p>All public methods are pure functions — they are stateless and produce no
 * side effects, making them safe for concurrent use.</p>
 */
@Component
public class ScaleNormalizer implements ScoreNormalizable {

    private static final ScaleNormalizer INSTANCE = new ScaleNormalizer();

    public static ScaleNormalizer getInstance() {
        return INSTANCE;
    }

    public ScaleNormalizer() {}

    // ── GPA Normalization ────────────────────────────────────────────────────

    /**
     * Normalizes a 10-point CGPA to the US 4.0 GPA scale and then to [0, 1].
     *
     * <p>Mapping: CGPA / 10.0 × 4.0 → clamped to [0, 4] → / 4.0 → [0, 1]</p>
     *
     * @param cgpa10pt applicant's CGPA on the 10-point scale (0–10)
     * @return normalized GPA in [0, 1]
     * @throws IllegalArgumentException if {@code cgpa10pt} is outside [0, 10]
     */
    public double normalizeToUS4pt(double cgpa10pt) {
        validateCgpa(cgpa10pt);
        double us4pt = (cgpa10pt / 10.0) * 4.0;
        return roundToTwoDecimals(Math.min(Math.max(us4pt, 0.0), 4.0));
    }

    /**
     * Normalizes a 10-point CGPA to a Japanese GPA scale using a piecewise function.
     *
     * <p>Piecewise mapping (to 4.0-equivalent):</p>
     * <ul>
     *   <li>cgpa10pt ≥ 9.0 → 4.0 (S-grade excellence)</li>
     *   <li>cgpa10pt ≥ 7.0 → linear interpolation from 3.0 to 4.0</li>
     *   <li>cgpa10pt ≥ 5.0 → linear interpolation from 2.0 to 3.0</li>
     *   <li>cgpa10pt  < 5.0 → linear interpolation from 0.0 to 2.0</li>
     * </ul>
     *
     * @param cgpa10pt applicant's CGPA on the 10-point scale (0–10)
     * @param jpScale  target Japanese scale maximum (typically 4.0)
     * @return equivalent GPA on Japanese scale [0.0, jpScale]
     * @throws IllegalArgumentException if {@code cgpa10pt} is outside [0, 10]
     */
    public double normalizeToJapaneseGpa(double cgpa10pt, double jpScale) {
        validateCgpa(cgpa10pt);
        double jpGpa;
        if (cgpa10pt >= 9.0) {
            jpGpa = 4.0;                                                           // S-grade
        } else if (cgpa10pt >= 7.0) {
            jpGpa = 3.0 + ((cgpa10pt - 7.0) / 2.0) * 1.0;                         // A-grade band
        } else if (cgpa10pt >= 5.0) {
            jpGpa = 2.0 + ((cgpa10pt - 5.0) / 2.0) * 1.0;                         // B-grade band
        } else {
            jpGpa = (cgpa10pt / 5.0) * 2.0;                                        // C/D-grade band
        }
        double scaled = (jpGpa / 4.0) * jpScale;
        return roundToTwoDecimals(Math.min(Math.max(scaled, 0.0), jpScale));
    }

    /**
     * Maps a 10-point CGPA to a European ECTS grade band and returns a normalized [0, 1] value.
     *
     * <p>Band mapping:</p>
     * <ul>
     *   <li>9.0–10.0 → A (1.0)</li>
     *   <li>7.5–8.9  → B (0.8)</li>
     *   <li>6.0–7.4  → C (0.6)</li>
     *   <li>5.0–5.9  → D (0.4)</li>
     *   <li> < 5.0   → F (0.0)</li>
     * </ul>
     *
     * @param cgpa10pt applicant's CGPA on the 10-point scale (0–10)
     * @return normalized ECTS grade value in [0, 1]
     * @throws IllegalArgumentException if {@code cgpa10pt} is outside [0, 10]
     */
    public double normalizeToEctsGrade(double cgpa10pt) {
        validateCgpa(cgpa10pt);
        if (cgpa10pt >= 9.0)      return 1.00;   // A
        else if (cgpa10pt >= 7.5) return 0.80;   // B
        else if (cgpa10pt >= 6.0) return 0.60;   // C
        else if (cgpa10pt >= 5.0) return 0.40;   // D
        else                      return 0.00;   // F
    }

    /**
     * Normalizes a GRE Verbal or Quantitative raw score (130–170) to [0, 1].
     *
     * @param rawScore GRE section score (130–170)
     * @return normalized score in [0, 1]; clamped if outside valid range
     */
    public double normalizeGreScore(int rawScore) {
        if (rawScore < 130 || rawScore > 170) {
            throw new IllegalArgumentException("GRE score must be between 130 and 170, received: " + rawScore);
        }
        return roundToTwoDecimals(clampedLinearNormalize(rawScore, 130.0, 170.0));
    }

    /**
     * Normalizes a GRE Analytical Writing score (0.0–6.0) to [0, 1].
     *
     * @param awScore AW score (0.0–6.0, in 0.5 increments)
     * @return normalized score in [0, 1]; clamped if outside valid range
     */
    public double normalizeGreAw(double awScore) {
        return roundToTwoDecimals(clampedLinearNormalize(awScore, 0.0, 6.0));
    }

    // ── Language Normalization ───────────────────────────────────────────────

    /**
     * Normalizes a JLPT level string to [0, 1].
     *
     * <p>JLPT levels are inversely numbered (N1 = hardest, N5 = easiest):</p>
     * <ul>
     *   <li>N1 → 1.00</li>
     *   <li>N2 → 0.80</li>
     *   <li>N3 → 0.60</li>
     *   <li>N4 → 0.40</li>
     *   <li>N5 → 0.20</li>
     *   <li>null / unknown → 0.00</li>
     * </ul>
     *
     * @param jlptLevel JLPT level string (e.g. "N1", "N2")
     * @return normalized JLPT proficiency in [0, 1]
     */
    public double normalizeJlpt(String jlptLevel) {
        if (jlptLevel == null) return 0.0;
        return switch (jlptLevel.trim().toUpperCase()) {
            case "N1" -> 1.00;
            case "N2" -> 0.80;
            case "N3" -> 0.60;
            case "N4" -> 0.40;
            case "N5" -> 0.20;
            default   -> 0.00;
        };
    }

    /**
     * Normalizes an IELTS Academic band score (0.0–9.0) to [0, 1].
     *
     * @param ieltsScore IELTS band score (0.0–9.0)
     * @return normalized IELTS score in [0, 1]; clamped if outside range
     */
    public double normalizeIelts(double ieltsScore) {
        return roundToTwoDecimals(clampedLinearNormalize(ieltsScore, 0.0, 9.0));
    }

    /**
     * Normalizes a TOEFL iBT score (0–120) to [0, 1].
     *
     * @param toeflScore TOEFL iBT total score (0–120)
     * @return normalized TOEFL score in [0, 1]; clamped if outside range
     */
    public double normalizeToefl(int toeflScore) {
        return roundToTwoDecimals(clampedLinearNormalize(toeflScore, 0.0, 120.0));
    }

    /**
     * Normalizes the applicant's ECTS CS credits relative to the program's threshold.
     *
     * <p>If applicant credits ≥ threshold, the result is capped at 1.0 (full credit coverage).
     * Values below threshold are linearly scaled.</p>
     *
     * @param applicantCredits number of ECTS credits the applicant has earned in CS subjects
     * @param thresholdCredits minimum ECTS credits required by the program
     * @return normalized ECTS coverage in [0, 1]
     */
    public double normalizeEctsCredits(int applicantCredits, int thresholdCredits) {
        if (thresholdCredits <= 0) return 1.0;
        double ratio = (double) applicantCredits / thresholdCredits;
        return roundToTwoDecimals(Math.min(ratio, 1.0));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Type-dispatch implementation supporting:</p>
     * <ul>
     *   <li>{@link String}  → JLPT level (e.g. "N2")</li>
     *   <li>{@link Double}  → IELTS band score</li>
     *   <li>{@link Integer} → TOEFL iBT score</li>
     * </ul>
     *
     * @throws IllegalArgumentException for unrecognized score types
     */
    @Override
    public double normalizeLanguage(Object score) {
        if (score == null)           return 0.0;
        if (score instanceof String  s) return normalizeJlpt(s);
        if (score instanceof Double  d) return normalizeIelts(d);
        if (score instanceof Integer i) return normalizeToefl(i);
        throw new IllegalArgumentException(
                "Unsupported language score type: " + score.getClass().getName());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link #clampedLinearNormalize} after extracting the numeric value
     * from the {@link Object} parameter.</p>
     *
     * @throws IllegalArgumentException if {@code raw} is not a {@link Number}
     */
    @Override
    public double normalizeToDomain(Object raw, double min, double max) {
        if (raw == null) return 0.0;
        if (!(raw instanceof Number n)) {
            throw new IllegalArgumentException(
                    "normalizeToDomain requires a Number; got: " + raw.getClass().getName());
        }
        return roundToTwoDecimals(clampedLinearNormalize(n.doubleValue(), min, max));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Equivalent to {@code normalizeToUS4pt(rawGpa)} when {@code sourceScale == 10.0},
     * otherwise performs a generic linear mapping.</p>
     */
    @Override
    public double normalizeGpa(double rawGpa, double sourceScale) {
        double normalized = rawGpa / sourceScale;
        return roundToTwoDecimals(Math.min(Math.max(normalized, 0.0), 1.0));
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    /**
     * Validates that a CGPA is within the legal [0, 10] range.
     *
     * @param cgpa the CGPA to validate
     * @throws IllegalArgumentException if {@code cgpa} is outside [0, 10]
     */
    private void validateCgpa(double cgpa) {
        if (cgpa < 0.0 || cgpa > 10.0) {
            throw new IllegalArgumentException(
                    "CGPA must be in [0, 10]; received: " + cgpa);
        }
    }

    /**
     * Linearly maps {@code value} from [min, max] to [0, 1], clamping extremes.
     *
     * @param value the raw value to normalize
     * @param min   the lower bound of the raw range (maps to 0.0)
     * @param max   the upper bound of the raw range (maps to 1.0)
     * @return normalized value in [0, 1]
     */
    private double clampedLinearNormalize(double value, double min, double max) {
        if (max == min) return 0.0;
        double normalized = (value - min) / (max - min);
        return Math.min(Math.max(normalized, 0.0), 1.0);
    }

    /**
     * Rounds a double to two decimal places using standard rounding rules.
     *
     * @param value the value to round
     * @return value rounded to two decimal places
     */
    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
