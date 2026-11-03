package com.admissions.engine.model.scholarship;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a major international scholarship, grant, or assistantship opportunity.
 */
public class ScholarshipOpportunity {

    private String id;
    private String name;
    private String organization;
    private String country;
    private String region;              // "GERMANY", "JAPAN", "US_UK", "EUROPE", "GLOBAL"
    private String coverageType;        // "FULL_RIDE", "TUITION_PLUS_STIPEND", "TUITION_WAIVER", "MONTHLY_STIPEND", "ASSISTANTSHIP"
    private double financialValueUsd;
    private String awardSummary;        // e.g. "100% Tuition Waiver + €1,400/mo stipend + travel"
    private double minCgpa;             // Normalized 10.0 scale minimum
    private int minWorkExperienceMonths;
    private boolean requiresThesis;
    private Integer minGreQuant;
    private Integer minToefl;
    private Double minIelts;
    private String requiresJlpt;
    private String applicationDeadline;
    private String applicationRoute;
    private String portalUrl;
    private List<String> eligibilityCriteria = new ArrayList<>();
    private List<String> keyTips = new ArrayList<>();
    private List<String> tags = new ArrayList<>();

    public ScholarshipOpportunity() {}

    public ScholarshipOpportunity(String id, String name, String organization, String country,
                                  String region, String coverageType, double financialValueUsd,
                                  String awardSummary, double minCgpa, int minWorkExperienceMonths,
                                  boolean requiresThesis, Integer minGreQuant, Integer minToefl,
                                  Double minIelts, String requiresJlpt, String applicationDeadline,
                                  String applicationRoute, String portalUrl,
                                  List<String> eligibilityCriteria, List<String> keyTips, List<String> tags) {
        this.id = id;
        this.name = name;
        this.organization = organization;
        this.country = country;
        this.region = region;
        this.coverageType = coverageType;
        this.financialValueUsd = financialValueUsd;
        this.awardSummary = awardSummary;
        this.minCgpa = minCgpa;
        this.minWorkExperienceMonths = minWorkExperienceMonths;
        this.requiresThesis = requiresThesis;
        this.minGreQuant = minGreQuant;
        this.minToefl = minToefl;
        this.minIelts = minIelts;
        this.requiresJlpt = requiresJlpt;
        this.applicationDeadline = applicationDeadline;
        this.applicationRoute = applicationRoute;
        this.portalUrl = portalUrl;
        this.eligibilityCriteria = eligibilityCriteria != null ? eligibilityCriteria : new ArrayList<>();
        this.keyTips = keyTips != null ? keyTips : new ArrayList<>();
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    // Getters and Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getCoverageType() { return coverageType; }
    public void setCoverageType(String coverageType) { this.coverageType = coverageType; }

    public double getFinancialValueUsd() { return financialValueUsd; }
    public void setFinancialValueUsd(double financialValueUsd) { this.financialValueUsd = financialValueUsd; }

    public String getAwardSummary() { return awardSummary; }
    public void setAwardSummary(String awardSummary) { this.awardSummary = awardSummary; }

    public double getMinCgpa() { return minCgpa; }
    public void setMinCgpa(double minCgpa) { this.minCgpa = minCgpa; }

    public int getMinWorkExperienceMonths() { return minWorkExperienceMonths; }
    public void setMinWorkExperienceMonths(int minWorkExperienceMonths) { this.minWorkExperienceMonths = minWorkExperienceMonths; }

    public boolean isRequiresThesis() { return requiresThesis; }
    public void setRequiresThesis(boolean requiresThesis) { this.requiresThesis = requiresThesis; }

    public Integer getMinGreQuant() { return minGreQuant; }
    public void setMinGreQuant(Integer minGreQuant) { this.minGreQuant = minGreQuant; }

    public Integer getMinToefl() { return minToefl; }
    public void setMinToefl(Integer minToefl) { this.minToefl = minToefl; }

    public Double getMinIelts() { return minIelts; }
    public void setMinIelts(Double minIelts) { this.minIelts = minIelts; }

    public String getRequiresJlpt() { return requiresJlpt; }
    public void setRequiresJlpt(String requiresJlpt) { this.requiresJlpt = requiresJlpt; }

    public String getApplicationDeadline() { return applicationDeadline; }
    public void setApplicationDeadline(String applicationDeadline) { this.applicationDeadline = applicationDeadline; }

    public String getApplicationRoute() { return applicationRoute; }
    public void setApplicationRoute(String applicationRoute) { this.applicationRoute = applicationRoute; }

    public String getPortalUrl() { return portalUrl; }
    public void setPortalUrl(String portalUrl) { this.portalUrl = portalUrl; }

    public List<String> getEligibilityCriteria() { return eligibilityCriteria; }
    public void setEligibilityCriteria(List<String> eligibilityCriteria) { this.eligibilityCriteria = eligibilityCriteria; }

    public List<String> getKeyTips() { return keyTips; }
    public void setKeyTips(List<String> keyTips) { this.keyTips = keyTips; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
