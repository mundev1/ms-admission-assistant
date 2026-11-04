package com.admissions.engine.model.ects;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete ECTS equivalence assessment report, prerequisite gaps, and remediation actions.
 */
public class EctsEvaluationResult {

    private String universityId;
    private String universityName;
    private String programName;
    private double totalEarnedIndianCredits;
    private double totalEarnedEcts;
    private double totalRequiredEcts;
    private double overallEctsCoveragePercent;
    private String overallVerdict; // ADMISSION_ELIGIBLE, CONDITIONAL_ADMISSION_RISK, HIGH_REJECTION_RISK
    private String executiveSummary;
    private String auflagenPolicyAdvisory;
    private Map<EctsCategory, CategoryCreditAnalysis> categoryAnalyses = new HashMap<>();
    private List<String> criticalDeficits = new ArrayList<>();
    private List<BridgeCourseRecommendation> recommendedBridgeCourses = new ArrayList<>();
    private String curriculumTableMarkdown;
    private LocalDate evaluatedAt = LocalDate.now();

    public EctsEvaluationResult() {}

    public EctsEvaluationResult(String universityId, String universityName, String programName,
                                double totalEarnedIndianCredits, double totalEarnedEcts,
                                double totalRequiredEcts, double overallEctsCoveragePercent,
                                String overallVerdict, String executiveSummary,
                                String auflagenPolicyAdvisory,
                                Map<EctsCategory, CategoryCreditAnalysis> categoryAnalyses,
                                List<String> criticalDeficits,
                                List<BridgeCourseRecommendation> recommendedBridgeCourses,
                                String curriculumTableMarkdown) {
        this.universityId = universityId;
        this.universityName = universityName;
        this.programName = programName;
        this.totalEarnedIndianCredits = totalEarnedIndianCredits;
        this.totalEarnedEcts = totalEarnedEcts;
        this.totalRequiredEcts = totalRequiredEcts;
        this.overallEctsCoveragePercent = overallEctsCoveragePercent;
        this.overallVerdict = overallVerdict;
        this.executiveSummary = executiveSummary;
        this.auflagenPolicyAdvisory = auflagenPolicyAdvisory;
        this.categoryAnalyses = categoryAnalyses != null ? categoryAnalyses : new HashMap<>();
        this.criticalDeficits = criticalDeficits != null ? criticalDeficits : new ArrayList<>();
        this.recommendedBridgeCourses = recommendedBridgeCourses != null ? recommendedBridgeCourses : new ArrayList<>();
        this.curriculumTableMarkdown = curriculumTableMarkdown;
        this.evaluatedAt = LocalDate.now();
    }

    public String getUniversityId() { return universityId; }
    public void setUniversityId(String universityId) { this.universityId = universityId; }

    public String getUniversityName() { return universityName; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }

    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }

    public double getTotalEarnedIndianCredits() { return totalEarnedIndianCredits; }
    public void setTotalEarnedIndianCredits(double totalEarnedIndianCredits) { this.totalEarnedIndianCredits = totalEarnedIndianCredits; }

    public double getTotalEarnedEcts() { return totalEarnedEcts; }
    public void setTotalEarnedEcts(double totalEarnedEcts) { this.totalEarnedEcts = totalEarnedEcts; }

    public double getTotalRequiredEcts() { return totalRequiredEcts; }
    public void setTotalRequiredEcts(double totalRequiredEcts) { this.totalRequiredEcts = totalRequiredEcts; }

    public double getOverallEctsCoveragePercent() { return overallEctsCoveragePercent; }
    public void setOverallEctsCoveragePercent(double overallEctsCoveragePercent) { this.overallEctsCoveragePercent = overallEctsCoveragePercent; }

    public String getOverallVerdict() { return overallVerdict; }
    public void setOverallVerdict(String overallVerdict) { this.overallVerdict = overallVerdict; }

    public String getExecutiveSummary() { return executiveSummary; }
    public void setExecutiveSummary(String executiveSummary) { this.executiveSummary = executiveSummary; }

    public String getAuflagenPolicyAdvisory() { return auflagenPolicyAdvisory; }
    public void setAuflagenPolicyAdvisory(String auflagenPolicyAdvisory) { this.auflagenPolicyAdvisory = auflagenPolicyAdvisory; }

    public Map<EctsCategory, CategoryCreditAnalysis> getCategoryAnalyses() { return categoryAnalyses; }
    public void setCategoryAnalyses(Map<EctsCategory, CategoryCreditAnalysis> categoryAnalyses) { this.categoryAnalyses = categoryAnalyses; }

    public List<String> getCriticalDeficits() { return criticalDeficits; }
    public void setCriticalDeficits(List<String> criticalDeficits) { this.criticalDeficits = criticalDeficits; }

    public List<BridgeCourseRecommendation> getRecommendedBridgeCourses() { return recommendedBridgeCourses; }
    public void setRecommendedBridgeCourses(List<BridgeCourseRecommendation> recommendedBridgeCourses) { this.recommendedBridgeCourses = recommendedBridgeCourses; }

    public String getCurriculumTableMarkdown() { return curriculumTableMarkdown; }
    public void setCurriculumTableMarkdown(String curriculumTableMarkdown) { this.curriculumTableMarkdown = curriculumTableMarkdown; }

    public LocalDate getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDate evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
