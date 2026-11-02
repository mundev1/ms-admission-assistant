package com.admissions.engine.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Value object encapsulating the complete result of scoring a single applicant
 * against a single university program, integrating Academic Compatibility,
 * Financial Feasibility, and Visa & Career policy scores into a Holistic Index.
 */
public class ScoreResult {

    private int applicantId;
    private int programId;
    private String universityName;
    private String programName;
    private String country;
    private String region;

    // Academic Compatibility (0-100)
    private double compatibilityScore;
    private String compatibilityTier;
    private NormalizedProfile normalizedProfile;
    private DeficitReport deficitReport;

    // Financial & Visa Overlay Metrics
    private double financialScore;
    private double visaScore;
    private double holisticScore;
    private String holisticTier;
    private boolean scholarshipEligible;
    private List<String> matchedScholarships = new ArrayList<>();
    private String financialSummary;
    private String visaSummary;

    // Cost & Visa details
    private Double tuitionAnnualUsd;
    private Double livingCostAnnualUsd;
    private Double blockedAccountRequirementUsd;
    private Integer postStudyVisaMonths;
    private String visaProgramName;

    private LocalDateTime calculationTimestamp = LocalDateTime.now();

    public ScoreResult() {}

    public ScoreResult(int applicantId, int programId, String universityName, String programName,
                       String country, String region, double compatibilityScore, String compatibilityTier,
                       NormalizedProfile normalizedProfile, DeficitReport deficitReport,
                       double financialScore, double visaScore, double holisticScore, String holisticTier,
                       boolean scholarshipEligible, List<String> matchedScholarships,
                       String financialSummary, String visaSummary,
                       Double tuitionAnnualUsd, Double livingCostAnnualUsd,
                       Double blockedAccountRequirementUsd, Integer postStudyVisaMonths,
                       String visaProgramName, LocalDateTime calculationTimestamp) {
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
        this.financialScore = financialScore;
        this.visaScore = visaScore;
        this.holisticScore = holisticScore;
        this.holisticTier = holisticTier;
        this.scholarshipEligible = scholarshipEligible;
        this.matchedScholarships = matchedScholarships != null ? matchedScholarships : new ArrayList<>();
        this.financialSummary = financialSummary;
        this.visaSummary = visaSummary;
        this.tuitionAnnualUsd = tuitionAnnualUsd;
        this.livingCostAnnualUsd = livingCostAnnualUsd;
        this.blockedAccountRequirementUsd = blockedAccountRequirementUsd;
        this.postStudyVisaMonths = postStudyVisaMonths;
        this.visaProgramName = visaProgramName;
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
        private double financialScore;
        private double visaScore;
        private double holisticScore;
        private String holisticTier;
        private boolean scholarshipEligible;
        private List<String> matchedScholarships = new ArrayList<>();
        private String financialSummary;
        private String visaSummary;
        private Double tuitionAnnualUsd;
        private Double livingCostAnnualUsd;
        private Double blockedAccountRequirementUsd;
        private Integer postStudyVisaMonths;
        private String visaProgramName;
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
        public ScoreResultBuilder financialScore(double financialScore) { this.financialScore = financialScore; return this; }
        public ScoreResultBuilder visaScore(double visaScore) { this.visaScore = visaScore; return this; }
        public ScoreResultBuilder holisticScore(double holisticScore) { this.holisticScore = holisticScore; return this; }
        public ScoreResultBuilder holisticTier(String holisticTier) { this.holisticTier = holisticTier; return this; }
        public ScoreResultBuilder scholarshipEligible(boolean scholarshipEligible) { this.scholarshipEligible = scholarshipEligible; return this; }
        public ScoreResultBuilder matchedScholarships(List<String> matchedScholarships) { this.matchedScholarships = matchedScholarships; return this; }
        public ScoreResultBuilder financialSummary(String financialSummary) { this.financialSummary = financialSummary; return this; }
        public ScoreResultBuilder visaSummary(String visaSummary) { this.visaSummary = visaSummary; return this; }
        public ScoreResultBuilder tuitionAnnualUsd(Double tuitionAnnualUsd) { this.tuitionAnnualUsd = tuitionAnnualUsd; return this; }
        public ScoreResultBuilder livingCostAnnualUsd(Double livingCostAnnualUsd) { this.livingCostAnnualUsd = livingCostAnnualUsd; return this; }
        public ScoreResultBuilder blockedAccountRequirementUsd(Double blockedAccountRequirementUsd) { this.blockedAccountRequirementUsd = blockedAccountRequirementUsd; return this; }
        public ScoreResultBuilder postStudyVisaMonths(Integer postStudyVisaMonths) { this.postStudyVisaMonths = postStudyVisaMonths; return this; }
        public ScoreResultBuilder visaProgramName(String visaProgramName) { this.visaProgramName = visaProgramName; return this; }
        public ScoreResultBuilder calculationTimestamp(LocalDateTime calculationTimestamp) { this.calculationTimestamp = calculationTimestamp; return this; }

        public ScoreResult build() {
            return new ScoreResult(applicantId, programId, universityName, programName, country, region,
                    compatibilityScore, compatibilityTier, normalizedProfile, deficitReport,
                    financialScore, visaScore, holisticScore, holisticTier, scholarshipEligible,
                    matchedScholarships, financialSummary, visaSummary,
                    tuitionAnnualUsd, livingCostAnnualUsd, blockedAccountRequirementUsd,
                    postStudyVisaMonths, visaProgramName, calculationTimestamp);
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

    public double getFinancialScore() { return financialScore; }
    public void setFinancialScore(double financialScore) { this.financialScore = financialScore; }

    public double getVisaScore() { return visaScore; }
    public void setVisaScore(double visaScore) { this.visaScore = visaScore; }

    public double getHolisticScore() { return holisticScore; }
    public void setHolisticScore(double holisticScore) { this.holisticScore = holisticScore; }

    public String getHolisticTier() { return holisticTier; }
    public void setHolisticTier(String holisticTier) { this.holisticTier = holisticTier; }

    public boolean isScholarshipEligible() { return scholarshipEligible; }
    public void setScholarshipEligible(boolean scholarshipEligible) { this.scholarshipEligible = scholarshipEligible; }

    public List<String> getMatchedScholarships() { return matchedScholarships; }
    public void setMatchedScholarships(List<String> matchedScholarships) { this.matchedScholarships = matchedScholarships; }

    public String getFinancialSummary() { return financialSummary; }
    public void setFinancialSummary(String financialSummary) { this.financialSummary = financialSummary; }

    public String getVisaSummary() { return visaSummary; }
    public void setVisaSummary(String visaSummary) { this.visaSummary = visaSummary; }

    public Double getTuitionAnnualUsd() { return tuitionAnnualUsd; }
    public void setTuitionAnnualUsd(Double tuitionAnnualUsd) { this.tuitionAnnualUsd = tuitionAnnualUsd; }

    public Double getLivingCostAnnualUsd() { return livingCostAnnualUsd; }
    public void setLivingCostAnnualUsd(Double livingCostAnnualUsd) { this.livingCostAnnualUsd = livingCostAnnualUsd; }

    public Double getBlockedAccountRequirementUsd() { return blockedAccountRequirementUsd; }
    public void setBlockedAccountRequirementUsd(Double blockedAccountRequirementUsd) { this.blockedAccountRequirementUsd = blockedAccountRequirementUsd; }

    public Integer getPostStudyVisaMonths() { return postStudyVisaMonths; }
    public void setPostStudyVisaMonths(Integer postStudyVisaMonths) { this.postStudyVisaMonths = postStudyVisaMonths; }

    public String getVisaProgramName() { return visaProgramName; }
    public void setVisaProgramName(String visaProgramName) { this.visaProgramName = visaProgramName; }

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
