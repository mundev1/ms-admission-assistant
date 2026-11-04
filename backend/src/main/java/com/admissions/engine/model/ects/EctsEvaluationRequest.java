package com.admissions.engine.model.ects;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.ArrayList;
import java.util.List;

/**
 * Request payload for evaluating Indian curriculum ECTS equivalence and prerequisite deficits.
 */
public class EctsEvaluationRequest {

    @JsonAlias({"universityId", "targetUniversity", "university"})
    private String targetUniversityId;

    private UniversityEctsRequirement customRequirement;

    @JsonAlias({"conversionMode", "mode"})
    private String conversionMode = "STANDARD_AICTE"; // STANDARD_AICTE, WORKLOAD_HOURS, RATIO_1_33, RATIO_1_5

    private double hoursPerEcts = 28.0; // 25.0 to 30.0 work hours per ECTS

    private List<SubjectEntry> subjects = new ArrayList<>();

    public EctsEvaluationRequest() {}

    public EctsEvaluationRequest(String targetUniversityId, UniversityEctsRequirement customRequirement,
                                String conversionMode, double hoursPerEcts, List<SubjectEntry> subjects) {
        this.targetUniversityId = targetUniversityId;
        this.customRequirement = customRequirement;
        this.conversionMode = conversionMode;
        this.hoursPerEcts = hoursPerEcts;
        this.subjects = subjects != null ? subjects : new ArrayList<>();
    }

    public String getTargetUniversityId() { return targetUniversityId; }
    public void setTargetUniversityId(String targetUniversityId) { this.targetUniversityId = targetUniversityId; }

    public UniversityEctsRequirement getCustomRequirement() { return customRequirement; }
    public void setCustomRequirement(UniversityEctsRequirement customRequirement) { this.customRequirement = customRequirement; }

    public String getConversionMode() { return conversionMode; }
    public void setConversionMode(String conversionMode) { this.conversionMode = conversionMode; }

    public double getHoursPerEcts() { return hoursPerEcts; }
    public void setHoursPerEcts(double hoursPerEcts) { this.hoursPerEcts = hoursPerEcts; }

    public List<SubjectEntry> getSubjects() { return subjects; }
    public void setSubjects(List<SubjectEntry> subjects) { this.subjects = subjects; }
}
