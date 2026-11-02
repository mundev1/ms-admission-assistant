package com.admissions.engine.model;

import java.time.LocalDateTime;

/**
 * Value object encapsulating the complete result of scoring a single applicant
 * against a single university program.
 */
public class ScoreResult {

    private int applicantId;
    private int programId;
    private String universityName;
    private String programName;
    private String country;
    private String region;
    private double compatibilityScore;
    private String compatibilityTier;
    private NormalizedProfile normalizedProfile;
    private DeficitReport deficitReport;
    private LocalDateTime calculationTimestamp = LocalDateTime.now();

    public ScoreResult() {}

    public ScoreResult(int applicantId, int programId, String universityName, String programName,
                       String country, String region, double compatibilityScore, String compatibilityTier,
                       NormalizedProfile normalizedProfile, DeficitReport deficitReport, LocalDateTime calculationTimestamp) {
        this.applicantId = applicantId;
        this.programId = programId;
        this.universityName = universityName;
        this.programName = programName;
        this.country = country;
        this.region = region;
        this.compatibilityScore = compatibilityScore;
        this.compatibilityTier = compatibilityTier;
        this.normalizedProfile = normalizedProfile;
        this.deficitReport = deficitReport;
        this.calculationTimestamp = calculationTimestamp != null ? calculationTimestamp : LocalDateTime.now();
    }

    public static ScoreResultBuilder builder() {
        return new ScoreResultBuilder();
    }

    public static class ScoreResultBuilder {
        private int applicantId;
        private int programId;
        private String universityName;
        private String programName;
        private String country;
        private String region;
        private double compatibilityScore;
        private String compatibilityTier;
        private NormalizedProfile normalizedProfile;
        private DeficitReport deficitReport;
        private LocalDateTime calculationTimestamp = LocalDateTime.now();

        public ScoreResultBuilder applicantId(int applicantId) { this.applicantId = applicantId; return this; }
        public ScoreResultBuilder programId(int programId) { this.programId = programId; return this; }
        public ScoreResultBuilder universityName(String universityName) { this.universityName = universityName; return this; }
        public ScoreResultBuilder programName(String programName) { this.programName = programName; return this; }
        public ScoreResultBuilder country(String country) { this.country = country; return this; }
        public ScoreResultBuilder region(String region) { this.region = region; return this; }
        public ScoreResultBuilder compatibilityScore(double compatibilityScore) { this.compatibilityScore = compatibilityScore; return this; }
        public ScoreResultBuilder compatibilityTier(String compatibilityTier) { this.compatibilityTier = compatibilityTier; return this; }
        public ScoreResultBuilder normalizedProfile(NormalizedProfile normalizedProfile) { this.normalizedProfile = normalizedProfile; return this; }
        public ScoreResultBuilder deficitReport(DeficitReport deficitReport) { this.deficitReport = deficitReport; return this; }
        public ScoreResultBuilder calculationTimestamp(LocalDateTime calculationTimestamp) { this.calculationTimestamp = calculationTimestamp; return this; }

        public ScoreResult build() {
            return new ScoreResult(applicantId, programId, universityName, programName, country, region,
                    compatibilityScore, compatibilityTier, normalizedProfile, deficitReport, calculationTimestamp);
        }
    }

    public int getApplicantId() { return applicantId; }
    public void setApplicantId(int applicantId) { this.applicantId = applicantId; }

    public int getProgramId() { return programId; }
    public void setProgramId(int programId) { this.programId = programId; }

    public String getUniversityName() { return universityName; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }

    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public double getCompatibilityScore() { return compatibilityScore; }
    public void setCompatibilityScore(double compatibilityScore) { this.compatibilityScore = compatibilityScore; }

    public double getOverallScore() { return Math.round(compatibilityScore); }

    public String getCompatibilityTier() { return compatibilityTier; }
    public void setCompatibilityTier(String compatibilityTier) { this.compatibilityTier = compatibilityTier; }

    public NormalizedProfile getNormalizedProfile() { return normalizedProfile; }
    public void setNormalizedProfile(NormalizedProfile normalizedProfile) { this.normalizedProfile = normalizedProfile; }

    public DeficitReport getDeficitReport() { return deficitReport; }
    public void setDeficitReport(DeficitReport deficitReport) { this.deficitReport = deficitReport; }

    public LocalDateTime getCalculationTimestamp() { return calculationTimestamp; }
    public void setCalculationTimestamp(LocalDateTime calculationTimestamp) { this.calculationTimestamp = calculationTimestamp; }

    public String getTierEmoji() {
        if (compatibilityTier == null) return "⚪";
        return switch (compatibilityTier.toUpperCase()) {
            case "SAFE"     -> "🟢";
            case "MATCH"    -> "🟡";
            case "REACH"    -> "🟠";
            case "UNLIKELY" -> "🔴";
            default         -> "⚪";
        };
    }
}
