package com.admissions.engine.model.timeline;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.LocalDate;

/**
 * Request payload for calculating a backwards-scheduled admissions and visa timeline.
 */
public class TimelineRequest {

    @JsonAlias({"targetIntake", "intake"})
    private String intakeId;

    @JsonAlias({"destinationCountry", "country"})
    private String targetCountry;

    private LocalDate targetStartDate;
    private Integer programId;
    private String targetUniversityName;
    private Boolean hasApsCertificate;
    private Boolean hasGreIelts;

    public TimelineRequest() {}

    public TimelineRequest(String intakeId, String targetCountry, LocalDate targetStartDate,
                           Integer programId, String targetUniversityName,
                           Boolean hasApsCertificate, Boolean hasGreIelts) {
        this.intakeId = intakeId;
        this.targetCountry = targetCountry;
        this.targetStartDate = targetStartDate;
        this.programId = programId;
        this.targetUniversityName = targetUniversityName;
        this.hasApsCertificate = hasApsCertificate;
        this.hasGreIelts = hasGreIelts;
    }

    public String getIntakeId() { return intakeId; }
    public void setIntakeId(String intakeId) { this.intakeId = intakeId; }

    public String getTargetCountry() { return targetCountry; }
    public void setTargetCountry(String targetCountry) { this.targetCountry = targetCountry; }

    public LocalDate getTargetStartDate() { return targetStartDate; }
    public void setTargetStartDate(LocalDate targetStartDate) { this.targetStartDate = targetStartDate; }

    public Integer getProgramId() { return programId; }
    public void setProgramId(Integer programId) { this.programId = programId; }

    public String getTargetUniversityName() { return targetUniversityName; }
    public void setTargetUniversityName(String targetUniversityName) { this.targetUniversityName = targetUniversityName; }

    public Boolean getHasApsCertificate() { return hasApsCertificate; }
    public void setHasApsCertificate(Boolean hasApsCertificate) { this.hasApsCertificate = hasApsCertificate; }

    public Boolean getHasGreIelts() { return hasGreIelts; }
    public void setHasGreIelts(Boolean hasGreIelts) { this.hasGreIelts = hasGreIelts; }
}
