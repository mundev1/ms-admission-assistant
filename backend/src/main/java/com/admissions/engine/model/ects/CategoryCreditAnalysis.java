package com.admissions.engine.model.ects;

import java.util.ArrayList;
import java.util.List;

/**
 * Detailed credit breakdown, requirement comparison, and deficit status for a single subject domain.
 */
public class CategoryCreditAnalysis {

    private EctsCategory category;
    private double earnedIndianCredits;
    private double earnedEcts;
    private double requiredEcts;
    private double deficitEcts;
    private double fulfillmentRatio;
    private String status; // SATISFIED, CONDITIONAL_GAP, CRITICAL_DEFICIT
    private List<String> contributingSubjects = new ArrayList<>();
    private String analysisMessage;

    public CategoryCreditAnalysis() {}

    public CategoryCreditAnalysis(EctsCategory category, double earnedIndianCredits, double earnedEcts,
                                  double requiredEcts, double deficitEcts, double fulfillmentRatio,
                                  String status, List<String> contributingSubjects, String analysisMessage) {
        this.category = category;
        this.earnedIndianCredits = earnedIndianCredits;
        this.earnedEcts = earnedEcts;
        this.requiredEcts = requiredEcts;
        this.deficitEcts = deficitEcts;
        this.fulfillmentRatio = fulfillmentRatio;
        this.status = status;
        this.contributingSubjects = contributingSubjects != null ? contributingSubjects : new ArrayList<>();
        this.analysisMessage = analysisMessage;
    }

    public EctsCategory getCategory() { return category; }
    public void setCategory(EctsCategory category) { this.category = category; }

    public double getEarnedIndianCredits() { return earnedIndianCredits; }
    public void setEarnedIndianCredits(double earnedIndianCredits) { this.earnedIndianCredits = earnedIndianCredits; }

    public double getEarnedEcts() { return earnedEcts; }
    public void setEarnedEcts(double earnedEcts) { this.earnedEcts = earnedEcts; }

    public double getRequiredEcts() { return requiredEcts; }
    public void setRequiredEcts(double requiredEcts) { this.requiredEcts = requiredEcts; }

    public double getDeficitEcts() { return deficitEcts; }
    public void setDeficitEcts(double deficitEcts) { this.deficitEcts = deficitEcts; }

    public double getFulfillmentRatio() { return fulfillmentRatio; }
    public void setFulfillmentRatio(double fulfillmentRatio) { this.fulfillmentRatio = fulfillmentRatio; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getContributingSubjects() { return contributingSubjects; }
    public void setContributingSubjects(List<String> contributingSubjects) { this.contributingSubjects = contributingSubjects; }

    public String getAnalysisMessage() { return analysisMessage; }
    public void setAnalysisMessage(String analysisMessage) { this.analysisMessage = analysisMessage; }
}
