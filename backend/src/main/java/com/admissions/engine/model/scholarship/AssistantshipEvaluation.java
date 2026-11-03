package com.admissions.engine.model.scholarship;

import java.util.ArrayList;
import java.util.List;

/**
 * Detailed evaluation of Graduate Teaching Assistantship (TA) and Research Assistantship (RA)
 * fee remission eligibility, stipend estimation, and work parameters.
 */
public class AssistantshipEvaluation {

    private boolean taEligible;
    private boolean raEligible;
    private double estimatedTuitionRemissionUsd;
    private double estimatedMonthlyStipendUsd;
    private double estimatedAnnualStipendUsd;
    private double estimatedTotalPackageUsd;
    private int weeklyWorkHours = 20;
    private String speakingExamStatus;
    private String researchReadinessStatus;
    private List<String> prerequisitesMet = new ArrayList<>();
    private List<String> prerequisitesPending = new ArrayList<>();
    private List<String> actionSteps = new ArrayList<>();

    public AssistantshipEvaluation() {}

    public AssistantshipEvaluation(boolean taEligible, boolean raEligible,
                                   double estimatedTuitionRemissionUsd, double estimatedMonthlyStipendUsd,
                                   double estimatedAnnualStipendUsd, double estimatedTotalPackageUsd,
                                   int weeklyWorkHours, String speakingExamStatus, String researchReadinessStatus,
                                   List<String> prerequisitesMet, List<String> prerequisitesPending,
                                   List<String> actionSteps) {
        this.taEligible = taEligible;
        this.raEligible = raEligible;
        this.estimatedTuitionRemissionUsd = estimatedTuitionRemissionUsd;
        this.estimatedMonthlyStipendUsd = estimatedMonthlyStipendUsd;
        this.estimatedAnnualStipendUsd = estimatedAnnualStipendUsd;
        this.estimatedTotalPackageUsd = estimatedTotalPackageUsd;
        this.weeklyWorkHours = weeklyWorkHours;
        this.speakingExamStatus = speakingExamStatus;
        this.researchReadinessStatus = researchReadinessStatus;
        this.prerequisitesMet = prerequisitesMet != null ? prerequisitesMet : new ArrayList<>();
        this.prerequisitesPending = prerequisitesPending != null ? prerequisitesPending : new ArrayList<>();
        this.actionSteps = actionSteps != null ? actionSteps : new ArrayList<>();
    }

    public boolean isTaEligible() { return taEligible; }
    public void setTaEligible(boolean taEligible) { this.taEligible = taEligible; }

    public boolean isRaEligible() { return raEligible; }
    public void setRaEligible(boolean raEligible) { this.raEligible = raEligible; }

    public double getEstimatedTuitionRemissionUsd() { return estimatedTuitionRemissionUsd; }
    public void setEstimatedTuitionRemissionUsd(double estimatedTuitionRemissionUsd) { this.estimatedTuitionRemissionUsd = estimatedTuitionRemissionUsd; }

    public double getEstimatedMonthlyStipendUsd() { return estimatedMonthlyStipendUsd; }
    public void setEstimatedMonthlyStipendUsd(double estimatedMonthlyStipendUsd) { this.estimatedMonthlyStipendUsd = estimatedMonthlyStipendUsd; }

    public double getEstimatedAnnualStipendUsd() { return estimatedAnnualStipendUsd; }
    public void setEstimatedAnnualStipendUsd(double estimatedAnnualStipendUsd) { this.estimatedAnnualStipendUsd = estimatedAnnualStipendUsd; }

    public double getEstimatedTotalPackageUsd() { return estimatedTotalPackageUsd; }
    public void setEstimatedTotalPackageUsd(double estimatedTotalPackageUsd) { this.estimatedTotalPackageUsd = estimatedTotalPackageUsd; }

    public int getWeeklyWorkHours() { return weeklyWorkHours; }
    public void setWeeklyWorkHours(int weeklyWorkHours) { this.weeklyWorkHours = weeklyWorkHours; }

    public String getSpeakingExamStatus() { return speakingExamStatus; }
    public void setSpeakingExamStatus(String speakingExamStatus) { this.speakingExamStatus = speakingExamStatus; }

    public String getResearchReadinessStatus() { return researchReadinessStatus; }
    public void setResearchReadinessStatus(String researchReadinessStatus) { this.researchReadinessStatus = researchReadinessStatus; }

    public List<String> getPrerequisitesMet() { return prerequisitesMet; }
    public void setPrerequisitesMet(List<String> prerequisitesMet) { this.prerequisitesMet = prerequisitesMet; }

    public List<String> getPrerequisitesPending() { return prerequisitesPending; }
    public void setPrerequisitesPending(List<String> prerequisitesPending) { this.prerequisitesPending = prerequisitesPending; }

    public List<String> getActionSteps() { return actionSteps; }
    public void setActionSteps(List<String> actionSteps) { this.actionSteps = actionSteps; }
}
