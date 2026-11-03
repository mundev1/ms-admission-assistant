package com.admissions.engine.model.alignment;

/**
 * Request payload containing the candidate's Statement of Purpose (SOP) or research proposal.
 */
public class AlignmentAnalysisRequest {

    private String sopText;
    private String targetUniversityName;
    private Integer programId;
    private String targetCountry;
    private String candidateName;
    private String undergradMajor;
    private String targetFacultyId;

    public AlignmentAnalysisRequest() {}

    public AlignmentAnalysisRequest(String sopText, String targetUniversityName, Integer programId,
                                    String targetCountry, String candidateName, String undergradMajor,
                                    String targetFacultyId) {
        this.sopText = sopText;
        this.targetUniversityName = targetUniversityName;
        this.programId = programId;
        this.targetCountry = targetCountry;
        this.candidateName = candidateName;
        this.undergradMajor = undergradMajor;
        this.targetFacultyId = targetFacultyId;
    }

    public String getSopText() { return sopText; }
    public void setSopText(String sopText) { this.sopText = sopText; }

    public String getTargetUniversityName() { return targetUniversityName; }
    public void setTargetUniversityName(String targetUniversityName) { this.targetUniversityName = targetUniversityName; }

    public Integer getProgramId() { return programId; }
    public void setProgramId(Integer programId) { this.programId = programId; }

    public String getTargetCountry() { return targetCountry; }
    public void setTargetCountry(String targetCountry) { this.targetCountry = targetCountry; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getUndergradMajor() { return undergradMajor; }
    public void setUndergradMajor(String undergradMajor) { this.undergradMajor = undergradMajor; }

    public String getTargetFacultyId() { return targetFacultyId; }
    public void setTargetFacultyId(String targetFacultyId) { this.targetFacultyId = targetFacultyId; }

    public void setFacultyId(String facultyId) { this.targetFacultyId = facultyId; }
}
