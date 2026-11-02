package com.admissions.engine.model;

import jakarta.persistence.*;

/**
 * JPA entity describing the academic framework and admission conventions
 * for a specific geographic region.
 */
@Entity
@Table(name = "country_frameworks")
public class CountryFramework {

    @Id
    @Column(name = "framework_id")
    private int frameworkId;

    @Column(name = "region")
    private String region;

    @Column(name = "gpa_scale")
    private Double gpaScale;

    @Column(name = "gpa_label")
    private String gpaLabel;

    @Column(name = "primary_test")
    private String primaryTest;

    @Column(name = "language_req")
    private String languageReq;

    @Column(name = "ects_required")
    private Boolean ectsRequired;

    @Column(name = "research_proposal")
    private Boolean researchProposal;

    @Column(name = "notes")
    private String notes;

    public CountryFramework() {}

    public CountryFramework(int frameworkId, String region, Double gpaScale, String gpaLabel,
                            String primaryTest, String languageReq, Boolean ectsRequired,
                            Boolean researchProposal, String notes) {
        this.frameworkId = frameworkId;
        this.region = region;
        this.gpaScale = gpaScale;
        this.gpaLabel = gpaLabel;
        this.primaryTest = primaryTest;
        this.languageReq = languageReq;
        this.ectsRequired = ectsRequired;
        this.researchProposal = researchProposal;
        this.notes = notes;
    }

    public static CountryFrameworkBuilder builder() {
        return new CountryFrameworkBuilder();
    }

    public static class CountryFrameworkBuilder {
        private int frameworkId;
        private String region;
        private Double gpaScale;
        private String gpaLabel;
        private String primaryTest;
        private String languageReq;
        private Boolean ectsRequired;
        private Boolean researchProposal;
        private String notes;

        public CountryFrameworkBuilder frameworkId(int frameworkId) { this.frameworkId = frameworkId; return this; }
        public CountryFrameworkBuilder region(String region) { this.region = region; return this; }
        public CountryFrameworkBuilder gpaScale(Double gpaScale) { this.gpaScale = gpaScale; return this; }
        public CountryFrameworkBuilder gpaLabel(String gpaLabel) { this.gpaLabel = gpaLabel; return this; }
        public CountryFrameworkBuilder primaryTest(String primaryTest) { this.primaryTest = primaryTest; return this; }
        public CountryFrameworkBuilder languageReq(String languageReq) { this.languageReq = languageReq; return this; }
        public CountryFrameworkBuilder ectsRequired(Boolean ectsRequired) { this.ectsRequired = ectsRequired; return this; }
        public CountryFrameworkBuilder researchProposal(Boolean researchProposal) { this.researchProposal = researchProposal; return this; }
        public CountryFrameworkBuilder notes(String notes) { this.notes = notes; return this; }

        public CountryFramework build() {
            return new CountryFramework(frameworkId, region, gpaScale, gpaLabel, primaryTest,
                    languageReq, ectsRequired, researchProposal, notes);
        }
    }

    public int getFrameworkId() { return frameworkId; }
    public void setFrameworkId(int frameworkId) { this.frameworkId = frameworkId; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Double getGpaScale() { return gpaScale; }
    public void setGpaScale(Double gpaScale) { this.gpaScale = gpaScale; }

    public String getGpaLabel() { return gpaLabel; }
    public void setGpaLabel(String gpaLabel) { this.gpaLabel = gpaLabel; }

    public String getPrimaryTest() { return primaryTest; }
    public void setPrimaryTest(String primaryTest) { this.primaryTest = primaryTest; }

    public String getLanguageReq() { return languageReq; }
    public void setLanguageReq(String languageReq) { this.languageReq = languageReq; }

    public Boolean getEctsRequired() { return ectsRequired; }
    public void setEctsRequired(Boolean ectsRequired) { this.ectsRequired = ectsRequired; }

    public Boolean getResearchProposal() { return researchProposal; }
    public void setResearchProposal(Boolean researchProposal) { this.researchProposal = researchProposal; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
