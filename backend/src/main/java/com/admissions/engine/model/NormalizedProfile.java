package com.admissions.engine.model;

/**
 * Immutable value object representing an applicant's academic credentials normalized
 * to the [0, 1] domain, ready for weighted scoring in a {@link com.admissions.engine.weighting.RegionalWeightingMatrix}.
 *
 * <p>Uses a manual inner {@link Builder} because Java records do not support
 * Lombok's {@code @Builder} natively.</p>
 *
 * @param normalizedGpa        GPA normalized to [0, 1]
 * @param normalizedGreVerbal  GRE Verbal score normalized to [0, 1]
 * @param normalizedGreQuant   GRE Quantitative score normalized to [0, 1]
 * @param normalizedGreAw      GRE Analytical Writing score normalized to [0, 1]
 * @param normalizedLanguage   Language proficiency score normalized to [0, 1]
 * @param normalizedEcts       ECTS credit coverage normalized to [0, 1]
 * @param normalizedResearch   Research quality score normalized to [0, 1]
 * @param normalizedCoursework Relevant coursework coverage normalized to [0, 1]
 * @param normalizedThesis     Thesis completion normalized to [0, 1] (binary: 0 or 1)
 */
public record NormalizedProfile(
        double normalizedGpa,
        double normalizedGreVerbal,
        double normalizedGreQuant,
        double normalizedGreAw,
        double normalizedLanguage,
        double normalizedEcts,
        double normalizedResearch,
        double normalizedCoursework,
        double normalizedThesis
) {

    /**
     * Returns a new {@link Builder} for constructing a {@link NormalizedProfile}.
     *
     * @return a fresh builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link NormalizedProfile}.
     *
     * <p>All fields default to {@code 0.0} if not explicitly set.</p>
     */
    public static final class Builder {

        private double normalizedGpa        = 0.0;
        private double normalizedGreVerbal  = 0.0;
        private double normalizedGreQuant   = 0.0;
        private double normalizedGreAw      = 0.0;
        private double normalizedLanguage   = 0.0;
        private double normalizedEcts       = 0.0;
        private double normalizedResearch   = 0.0;
        private double normalizedCoursework = 0.0;
        private double normalizedThesis     = 0.0;

        private Builder() {}

        /** @param v normalized GPA value [0, 1]; @return this builder */
        public Builder normalizedGpa(double v)        { this.normalizedGpa = v;        return this; }

        /** @param v normalized GRE Verbal value [0, 1]; @return this builder */
        public Builder normalizedGreVerbal(double v)  { this.normalizedGreVerbal = v;  return this; }

        /** @param v normalized GRE Quant value [0, 1]; @return this builder */
        public Builder normalizedGreQuant(double v)   { this.normalizedGreQuant = v;   return this; }

        /** @param v normalized GRE AW value [0, 1]; @return this builder */
        public Builder normalizedGreAw(double v)      { this.normalizedGreAw = v;      return this; }

        /** @param v normalized language score [0, 1]; @return this builder */
        public Builder normalizedLanguage(double v)   { this.normalizedLanguage = v;   return this; }

        /** @param v normalized ECTS coverage [0, 1]; @return this builder */
        public Builder normalizedEcts(double v)       { this.normalizedEcts = v;       return this; }

        /** @param v normalized research score [0, 1]; @return this builder */
        public Builder normalizedResearch(double v)   { this.normalizedResearch = v;   return this; }

        /** @param v normalized coursework coverage [0, 1]; @return this builder */
        public Builder normalizedCoursework(double v) { this.normalizedCoursework = v; return this; }

        /** @param v normalized thesis score [0, 1]; @return this builder */
        public Builder normalizedThesis(double v)     { this.normalizedThesis = v;     return this; }

        /**
         * Builds and returns an immutable {@link NormalizedProfile}.
         *
         * @return a new {@code NormalizedProfile} with the configured values
         */
        public NormalizedProfile build() {
            return new NormalizedProfile(
                    normalizedGpa,
                    normalizedGreVerbal,
                    normalizedGreQuant,
                    normalizedGreAw,
                    normalizedLanguage,
                    normalizedEcts,
                    normalizedResearch,
                    normalizedCoursework,
                    normalizedThesis
            );
        }
    }
}
