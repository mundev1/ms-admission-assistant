package com.admissions.engine.model.scholarship;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of evaluating an applicant's profile against an individual scholarship opportunity.
 */
public class ScholarshipMatchResult {

    private ScholarshipOpportunity opportunity;
    private double matchScore;                  // 0.0 - 100.0%
    private String eligibilityStatus;           // "DIRECT_MATCH", "STRONG_CANDIDATE", "COMPETITIVE_WITH_GAP", "NOT_ELIGIBLE"
    private String statusLabel;                 // User-friendly badge text
    private double estimatedAwardValueUsd;
    private List<String> satisfiedRequirements = new ArrayList<>();
    private List<String> unmetRequirements = new ArrayList<>();
    private List<String> actionableAdvice = new ArrayList<>();

    public ScholarshipMatchResult() {}

    public ScholarshipMatchResult(ScholarshipOpportunity opportunity, double matchScore,
                                  String eligibilityStatus, String statusLabel, double estimatedAwardValueUsd,
                                  List<String> satisfiedRequirements, List<String> unmetRequirements,
                                  List<String> actionableAdvice) {
        this.opportunity = opportunity;
        this.matchScore = matchScore;
        this.eligibilityStatus = eligibilityStatus;
        this.statusLabel = statusLabel;
        this.estimatedAwardValueUsd = estimatedAwardValueUsd;
        this.satisfiedRequirements = satisfiedRequirements != null ? satisfiedRequirements : new ArrayList<>();
        this.unmetRequirements = unmetRequirements != null ? unmetRequirements : new ArrayList<>();
        this.actionableAdvice = actionableAdvice != null ? actionableAdvice : new ArrayList<>();
    }

    public ScholarshipOpportunity getOpportunity() { return opportunity; }
    public void setOpportunity(ScholarshipOpportunity opportunity) { this.opportunity = opportunity; }

    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }

    public String getEligibilityStatus() { return eligibilityStatus; }
    public void setEligibilityStatus(String eligibilityStatus) { this.eligibilityStatus = eligibilityStatus; }

    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }

    public double getEstimatedAwardValueUsd() { return estimatedAwardValueUsd; }
    public void setEstimatedAwardValueUsd(double estimatedAwardValueUsd) { this.estimatedAwardValueUsd = estimatedAwardValueUsd; }

    public List<String> getSatisfiedRequirements() { return satisfiedRequirements; }
    public void setSatisfiedRequirements(List<String> satisfiedRequirements) { this.satisfiedRequirements = satisfiedRequirements; }

    public List<String> getUnmetRequirements() { return unmetRequirements; }
    public void setUnmetRequirements(List<String> unmetRequirements) { this.unmetRequirements = unmetRequirements; }

    public List<String> getActionableAdvice() { return actionableAdvice; }
    public void setActionableAdvice(List<String> actionableAdvice) { this.actionableAdvice = actionableAdvice; }
}
