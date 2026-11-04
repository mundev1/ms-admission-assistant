package com.admissions.engine.model.ects;

import java.util.ArrayList;
import java.util.List;

/**
 * Strict departmental prerequisite and ECTS quota requirements for a German university program.
 */
public class UniversityEctsRequirement {

    private String id;
    private String universityName;
    private String programName;
    private String state;
    private double minTheoreticalCsEcts;
    private double minMathematicsEcts;
    private double minPracticalSystemsEcts;
    private double minSoftwareEngEcts;
    private double minTotalCsEcts;
    private double maxAllowableDeficitEcts;
    private String admissionsRuleNotes;
    private List<String> mandatoryCoreTopics = new ArrayList<>();

    public UniversityEctsRequirement() {}

    public UniversityEctsRequirement(String id, String universityName, String programName, String state,
                                    double minTheoreticalCsEcts, double minMathematicsEcts,
                                    double minPracticalSystemsEcts, double minSoftwareEngEcts,
                                    double minTotalCsEcts, double maxAllowableDeficitEcts,
                                    String admissionsRuleNotes, List<String> mandatoryCoreTopics) {
        this.id = id;
        this.universityName = universityName;
        this.programName = programName;
        this.state = state;
        this.minTheoreticalCsEcts = minTheoreticalCsEcts;
        this.minMathematicsEcts = minMathematicsEcts;
        this.minPracticalSystemsEcts = minPracticalSystemsEcts;
        this.minSoftwareEngEcts = minSoftwareEngEcts;
        this.minTotalCsEcts = minTotalCsEcts;
        this.maxAllowableDeficitEcts = maxAllowableDeficitEcts;
        this.admissionsRuleNotes = admissionsRuleNotes;
        this.mandatoryCoreTopics = mandatoryCoreTopics != null ? mandatoryCoreTopics : new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUniversityName() { return universityName; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }

    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public double getMinTheoreticalCsEcts() { return minTheoreticalCsEcts; }
    public void setMinTheoreticalCsEcts(double minTheoreticalCsEcts) { this.minTheoreticalCsEcts = minTheoreticalCsEcts; }

    public double getMinMathematicsEcts() { return minMathematicsEcts; }
    public void setMinMathematicsEcts(double minMathematicsEcts) { this.minMathematicsEcts = minMathematicsEcts; }

    public double getMinPracticalSystemsEcts() { return minPracticalSystemsEcts; }
    public void setMinPracticalSystemsEcts(double minPracticalSystemsEcts) { this.minPracticalSystemsEcts = minPracticalSystemsEcts; }

    public double getMinSoftwareEngEcts() { return minSoftwareEngEcts; }
    public void setMinSoftwareEngEcts(double minSoftwareEngEcts) { this.minSoftwareEngEcts = minSoftwareEngEcts; }

    public double getMinTotalCsEcts() { return minTotalCsEcts; }
    public void setMinTotalCsEcts(double minTotalCsEcts) { this.minTotalCsEcts = minTotalCsEcts; }

    public double getMaxAllowableDeficitEcts() { return maxAllowableDeficitEcts; }
    public void setMaxAllowableDeficitEcts(double maxAllowableDeficitEcts) { this.maxAllowableDeficitEcts = maxAllowableDeficitEcts; }

    public String getAdmissionsRuleNotes() { return admissionsRuleNotes; }
    public void setAdmissionsRuleNotes(String admissionsRuleNotes) { this.admissionsRuleNotes = admissionsRuleNotes; }

    public List<String> getMandatoryCoreTopics() { return mandatoryCoreTopics; }
    public void setMandatoryCoreTopics(List<String> mandatoryCoreTopics) { this.mandatoryCoreTopics = mandatoryCoreTopics; }
}
