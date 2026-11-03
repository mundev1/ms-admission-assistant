package com.admissions.engine.model.scholarship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the overall radar scan results across all global scholarships,
 * categorized matches, statistics, and strategic recommendations.
 */
public class ScholarshipRadarResult {

    private String applicantName;
    private double totalEligibleScholarshipValueUsd;
    private int totalQualifiedAwards;
    private int totalStrongCandidateAwards;
    private int totalAwardsEvaluated;
    private int fullRideCount;
    private Map<String, List<ScholarshipMatchResult>> categorizedMatches = new HashMap<>();
    private List<ScholarshipMatchResult> allMatches = new ArrayList<>();
    private AssistantshipEvaluation assistantshipEvaluation;
    private List<String> strategicRecommendations = new ArrayList<>();

    public ScholarshipRadarResult() {}

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public double getTotalEligibleScholarshipValueUsd() { return totalEligibleScholarshipValueUsd; }
    public void setTotalEligibleScholarshipValueUsd(double totalEligibleScholarshipValueUsd) { this.totalEligibleScholarshipValueUsd = totalEligibleScholarshipValueUsd; }

    public int getTotalQualifiedAwards() { return totalQualifiedAwards; }
    public void setTotalQualifiedAwards(int totalQualifiedAwards) { this.totalQualifiedAwards = totalQualifiedAwards; }

    public int getTotalStrongCandidateAwards() { return totalStrongCandidateAwards; }
    public void setTotalStrongCandidateAwards(int totalStrongCandidateAwards) { this.totalStrongCandidateAwards = totalStrongCandidateAwards; }

    public int getTotalAwardsEvaluated() { return totalAwardsEvaluated; }
    public void setTotalAwardsEvaluated(int totalAwardsEvaluated) { this.totalAwardsEvaluated = totalAwardsEvaluated; }

    public int getFullRideCount() { return fullRideCount; }
    public void setFullRideCount(int fullRideCount) { this.fullRideCount = fullRideCount; }

    public Map<String, List<ScholarshipMatchResult>> getCategorizedMatches() { return categorizedMatches; }
    public void setCategorizedMatches(Map<String, List<ScholarshipMatchResult>> categorizedMatches) { this.categorizedMatches = categorizedMatches; }

    public List<ScholarshipMatchResult> getAllMatches() { return allMatches; }
    public void setAllMatches(List<ScholarshipMatchResult> allMatches) { this.allMatches = allMatches; }

    public AssistantshipEvaluation getAssistantshipEvaluation() { return assistantshipEvaluation; }
    public void setAssistantshipEvaluation(AssistantshipEvaluation assistantshipEvaluation) { this.assistantshipEvaluation = assistantshipEvaluation; }

    public List<String> getStrategicRecommendations() { return strategicRecommendations; }
    public void setStrategicRecommendations(List<String> strategicRecommendations) { this.strategicRecommendations = strategicRecommendations; }
}
