package com.admissions.engine.model.timeline;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * An actionable admissions or visa milestone calculated through backwards scheduling.
 */
public class TimelineMilestone {

    private String id;
    private String title;
    private String description;
    private TimelinePhase phase;
    private MilestoneCriticality criticality;
    private MilestoneStatus status;
    private LocalDate targetDate;
    private int daysOffsetFromStart;
    private String leadTimeDescription;
    private String indianStudentAdvisory;
    private List<String> actionChecklist = new ArrayList<>();
    private String externalResourceUrl;
    private String googleCalendarUrl;

    public TimelineMilestone() {}

    public TimelineMilestone(String id, String title, String description, TimelinePhase phase,
                             MilestoneCriticality criticality, MilestoneStatus status,
                             LocalDate targetDate, int daysOffsetFromStart, String leadTimeDescription,
                             String indianStudentAdvisory, List<String> actionChecklist,
                             String externalResourceUrl, String googleCalendarUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.phase = phase;
        this.criticality = criticality;
        this.status = status;
        this.targetDate = targetDate;
        this.daysOffsetFromStart = daysOffsetFromStart;
        this.leadTimeDescription = leadTimeDescription;
        this.indianStudentAdvisory = indianStudentAdvisory;
        this.actionChecklist = actionChecklist != null ? actionChecklist : new ArrayList<>();
        this.externalResourceUrl = externalResourceUrl;
        this.googleCalendarUrl = googleCalendarUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TimelinePhase getPhase() { return phase; }
    public void setPhase(TimelinePhase phase) { this.phase = phase; }

    public MilestoneCriticality getCriticality() { return criticality; }
    public void setCriticality(MilestoneCriticality criticality) { this.criticality = criticality; }

    public MilestoneStatus getStatus() { return status; }
    public void setStatus(MilestoneStatus status) { this.status = status; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

    public int getDaysOffsetFromStart() { return daysOffsetFromStart; }
    public void setDaysOffsetFromStart(int daysOffsetFromStart) { this.daysOffsetFromStart = daysOffsetFromStart; }

    public String getLeadTimeDescription() { return leadTimeDescription; }
    public void setLeadTimeDescription(String leadTimeDescription) { this.leadTimeDescription = leadTimeDescription; }

    public String getIndianStudentAdvisory() { return indianStudentAdvisory; }
    public void setIndianStudentAdvisory(String indianStudentAdvisory) { this.indianStudentAdvisory = indianStudentAdvisory; }

    public List<String> getActionChecklist() { return actionChecklist; }
    public void setActionChecklist(List<String> actionChecklist) { this.actionChecklist = actionChecklist; }

    public String getExternalResourceUrl() { return externalResourceUrl; }
    public void setExternalResourceUrl(String externalResourceUrl) { this.externalResourceUrl = externalResourceUrl; }

    public String getGoogleCalendarUrl() { return googleCalendarUrl; }
    public void setGoogleCalendarUrl(String googleCalendarUrl) { this.googleCalendarUrl = googleCalendarUrl; }
}
