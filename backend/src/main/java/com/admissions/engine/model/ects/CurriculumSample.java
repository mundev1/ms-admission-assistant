package com.admissions.engine.model.ects;

import java.util.ArrayList;
import java.util.List;

/**
 * Standard pre-loaded Indian university curriculum template (e.g. AICTE Model, AKTU/COER, Tier-2/3).
 */
public class CurriculumSample {

    private String id;
    private String name;
    private String institutionType;
    private String description;
    private double totalIndianCredits;
    private List<SubjectEntry> subjects = new ArrayList<>();

    public CurriculumSample() {}

    public CurriculumSample(String id, String name, String institutionType,
                            String description, double totalIndianCredits, List<SubjectEntry> subjects) {
        this.id = id;
        this.name = name;
        this.institutionType = institutionType;
        this.description = description;
        this.totalIndianCredits = totalIndianCredits;
        this.subjects = subjects != null ? subjects : new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getInstitutionType() { return institutionType; }
    public void setInstitutionType(String institutionType) { this.institutionType = institutionType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getTotalIndianCredits() { return totalIndianCredits; }
    public void setTotalIndianCredits(double totalIndianCredits) { this.totalIndianCredits = totalIndianCredits; }

    public List<SubjectEntry> getSubjects() { return subjects; }
    public void setSubjects(List<SubjectEntry> subjects) { this.subjects = subjects; }
}
