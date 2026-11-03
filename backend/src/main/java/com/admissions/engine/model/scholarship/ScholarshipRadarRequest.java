package com.admissions.engine.model.scholarship;

import com.admissions.engine.model.ApplicantProfile;

/**
 * Incoming request payload for evaluating an applicant profile against global scholarships.
 */
public class ScholarshipRadarRequest {

    private ApplicantProfile profile;
    private String targetRegion = "ALL";        // "ALL", "GERMANY", "JAPAN", "US_UK", "EUROPE"
    private String minCoverageType = "ALL";     // "ALL", "FULL_RIDE", "TUITION_PLUS_STIPEND", "ASSISTANTSHIP"

    public ScholarshipRadarRequest() {}

    public ScholarshipRadarRequest(ApplicantProfile profile, String targetRegion, String minCoverageType) {
        this.profile = profile;
        this.targetRegion = targetRegion != null ? targetRegion : "ALL";
        this.minCoverageType = minCoverageType != null ? minCoverageType : "ALL";
    }

    public ApplicantProfile getProfile() { return profile; }
    public void setProfile(ApplicantProfile profile) { this.profile = profile; }

    public String getTargetRegion() { return targetRegion; }
    public void setTargetRegion(String targetRegion) { this.targetRegion = targetRegion; }

    public String getMinCoverageType() { return minCoverageType; }
    public void setMinCoverageType(String minCoverageType) { this.minCoverageType = minCoverageType; }
}
