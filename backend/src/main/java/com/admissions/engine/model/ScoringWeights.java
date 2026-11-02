package com.admissions.engine.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a set of scoring weights for a particular regional admissions framework.
 */
@Entity
@Table(name = "scoring_weights")
public class ScoringWeights {

    @Id
    @Column(name = "weight_id")
    private int weightId;

    @Column(name = "framework_id")
    private int frameworkId;

    @Column(name = "weight_version")
    private String weightVersion;

    @Column(name = "weight_gpa")
    private Double weightGpa;

    @Column(name = "weight_gre_verbal")
    private Double weightGreVerbal;

    @Column(name = "weight_gre_quant")
    private Double weightGreQuant;

    @Column(name = "weight_gre_aw")
    private Double weightGreAw;

    @Column(name = "weight_language")
    private Double weightLanguage;

    @Column(name = "weight_ects")
    private Double weightEcts;

    @Column(name = "weight_research")
    private Double weightResearch;

    @Column(name = "weight_coursework")
    private Double weightCoursework;

    @Column(name = "weight_thesis")
    private Double weightThesis;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "notes")
    private String notes;

    public ScoringWeights() {}

    public ScoringWeights(int weightId, int frameworkId, String weightVersion, Double weightGpa,
                          Double weightGreVerbal, Double weightGreQuant, Double weightGreAw,
                          Double weightLanguage, Double weightEcts, Double weightResearch,
                          Double weightCoursework, Double weightThesis, Boolean isActive, String notes) {
        this.weightId = weightId;
        this.frameworkId = frameworkId;
        this.weightVersion = weightVersion;
        this.weightGpa = weightGpa;
        this.weightGreVerbal = weightGreVerbal;
        this.weightGreQuant = weightGreQuant;
        this.weightGreAw = weightGreAw;
        this.weightLanguage = weightLanguage;
        this.weightEcts = weightEcts;
        this.weightResearch = weightResearch;
        this.weightCoursework = weightCoursework;
        this.weightThesis = weightThesis;
        this.isActive = isActive;
        this.notes = notes;
    }

    public static ScoringWeightsBuilder builder() {
        return new ScoringWeightsBuilder();
    }

    public static class ScoringWeightsBuilder {
        private int weightId;
        private int frameworkId;
        private String weightVersion;
        private Double weightGpa;
        private Double weightGreVerbal;
        private Double weightGreQuant;
        private Double weightGreAw;
        private Double weightLanguage;
        private Double weightEcts;
        private Double weightResearch;
        private Double weightCoursework;
        private Double weightThesis;
        private Boolean isActive;
        private String notes;

        public ScoringWeightsBuilder weightId(int weightId) { this.weightId = weightId; return this; }
        public ScoringWeightsBuilder frameworkId(int frameworkId) { this.frameworkId = frameworkId; return this; }
        public ScoringWeightsBuilder weightVersion(String weightVersion) { this.weightVersion = weightVersion; return this; }
        public ScoringWeightsBuilder weightGpa(Double weightGpa) { this.weightGpa = weightGpa; return this; }
        public ScoringWeightsBuilder weightGreVerbal(Double weightGreVerbal) { this.weightGreVerbal = weightGreVerbal; return this; }
        public ScoringWeightsBuilder weightGreQuant(Double weightGreQuant) { this.weightGreQuant = weightGreQuant; return this; }
        public ScoringWeightsBuilder weightGreAw(Double weightGreAw) { this.weightGreAw = weightGreAw; return this; }
        public ScoringWeightsBuilder weightLanguage(Double weightLanguage) { this.weightLanguage = weightLanguage; return this; }
        public ScoringWeightsBuilder weightEcts(Double weightEcts) { this.weightEcts = weightEcts; return this; }
        public ScoringWeightsBuilder weightResearch(Double weightResearch) { this.weightResearch = weightResearch; return this; }
        public ScoringWeightsBuilder weightCoursework(Double weightCoursework) { this.weightCoursework = weightCoursework; return this; }
        public ScoringWeightsBuilder weightThesis(Double weightThesis) { this.weightThesis = weightThesis; return this; }
        public ScoringWeightsBuilder isActive(Boolean isActive) { this.isActive = isActive; return this; }
        public ScoringWeightsBuilder notes(String notes) { this.notes = notes; return this; }

        public ScoringWeights build() {
            return new ScoringWeights(weightId, frameworkId, weightVersion, weightGpa, weightGreVerbal,
                    weightGreQuant, weightGreAw, weightLanguage, weightEcts, weightResearch,
                    weightCoursework, weightThesis, isActive, notes);
        }
    }

    public void validate() {
        double total = safeAdd(weightGpa, weightGreVerbal, weightGreQuant,
                weightGreAw, weightLanguage, weightEcts,
                weightResearch, weightCoursework, weightThesis);

        if (Math.abs(total - 1.0) > 0.001) {
            throw new IllegalStateException(
                    String.format("ScoringWeights (weightId=%d, version=%s) do not sum to 1.0 — actual sum: %.6f",
                            weightId, weightVersion, total));
        }
    }

    private double safeAdd(Double... values) {
        double sum = 0.0;
        for (Double v : values) {
            if (v != null) {
                sum += v;
            }
        }
        return sum;
    }

    public int getWeightId() { return weightId; }
    public void setWeightId(int weightId) { this.weightId = weightId; }

    public int getFrameworkId() { return frameworkId; }
    public void setFrameworkId(int frameworkId) { this.frameworkId = frameworkId; }

    public String getWeightVersion() { return weightVersion; }
    public void setWeightVersion(String weightVersion) { this.weightVersion = weightVersion; }

    public Double getWeightGpa() { return weightGpa; }
    public void setWeightGpa(Double weightGpa) { this.weightGpa = weightGpa; }

    public Double getWeightGreVerbal() { return weightGreVerbal; }
    public void setWeightGreVerbal(Double weightGreVerbal) { this.weightGreVerbal = weightGreVerbal; }

    public Double getWeightGreQuant() { return weightGreQuant; }
    public void setWeightGreQuant(Double weightGreQuant) { this.weightGreQuant = weightGreQuant; }

    public Double getWeightGreAw() { return weightGreAw; }
    public void setWeightGreAw(Double weightGreAw) { this.weightGreAw = weightGreAw; }

    public Double getWeightLanguage() { return weightLanguage; }
    public void setWeightLanguage(Double weightLanguage) { this.weightLanguage = weightLanguage; }

    public Double getWeightEcts() { return weightEcts; }
    public void setWeightEcts(Double weightEcts) { this.weightEcts = weightEcts; }

    public Double getWeightResearch() { return weightResearch; }
    public void setWeightResearch(Double weightResearch) { this.weightResearch = weightResearch; }

    public Double getWeightCoursework() { return weightCoursework; }
    public void setWeightCoursework(Double weightCoursework) { this.weightCoursework = weightCoursework; }

    public Double getWeightThesis() { return weightThesis; }
    public void setWeightThesis(Double weightThesis) { this.weightThesis = weightThesis; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
