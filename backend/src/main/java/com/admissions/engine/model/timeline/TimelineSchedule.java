package com.admissions.engine.model.timeline;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Complete timeline schedule computed by the backwards scheduling planner.
 */
public class TimelineSchedule {

    private String intakeId;
    private String intakeName;
    private String country;
    private LocalDate targetStartDate;
    private int totalMilestones;
    private int criticalPathCount;
    private int overdueCount;
    private int urgentCount;
    private String summaryAdvisory;
    private List<String> criticalAlerts = new ArrayList<>();
    private List<TimelineMilestone> milestones = new ArrayList<>();
    private String icsCalendarContent;
    private LocalDate generatedAt = LocalDate.now();

    public TimelineSchedule() {}

    public TimelineSchedule(String intakeId, String intakeName, String country,
                            LocalDate targetStartDate, int totalMilestones, int criticalPathCount,
                            int overdueCount, int urgentCount, String summaryAdvisory,
                            List<String> criticalAlerts, List<TimelineMilestone> milestones,
                            String icsCalendarContent) {
        this.intakeId = intakeId;
        this.intakeName = intakeName;
        this.country = country;
        this.targetStartDate = targetStartDate;
        this.totalMilestones = totalMilestones;
        this.criticalPathCount = criticalPathCount;
        this.overdueCount = overdueCount;
        this.urgentCount = urgentCount;
        this.summaryAdvisory = summaryAdvisory;
        this.criticalAlerts = criticalAlerts != null ? criticalAlerts : new ArrayList<>();
        this.milestones = milestones != null ? milestones : new ArrayList<>();
        this.icsCalendarContent = icsCalendarContent;
        this.generatedAt = LocalDate.now();
    }

    public String getIntakeId() { return intakeId; }
    public void setIntakeId(String intakeId) { this.intakeId = intakeId; }

    public String getIntakeName() { return intakeName; }
    public void setIntakeName(String intakeName) { this.intakeName = intakeName; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public LocalDate getTargetStartDate() { return targetStartDate; }
    public void setTargetStartDate(LocalDate targetStartDate) { this.targetStartDate = targetStartDate; }

    public int getTotalMilestones() { return totalMilestones; }
    public void setTotalMilestones(int totalMilestones) { this.totalMilestones = totalMilestones; }

    public int getCriticalPathCount() { return criticalPathCount; }
    public void setCriticalPathCount(int criticalPathCount) { this.criticalPathCount = criticalPathCount; }

    public int getOverdueCount() { return overdueCount; }
    public void setOverdueCount(int overdueCount) { this.overdueCount = overdueCount; }

    public int getUrgentCount() { return urgentCount; }
    public void setUrgentCount(int urgentCount) { this.urgentCount = urgentCount; }

    public String getSummaryAdvisory() { return summaryAdvisory; }
    public void setSummaryAdvisory(String summaryAdvisory) { this.summaryAdvisory = summaryAdvisory; }

    public List<String> getCriticalAlerts() { return criticalAlerts; }
    public void setCriticalAlerts(List<String> criticalAlerts) { this.criticalAlerts = criticalAlerts; }

    public List<TimelineMilestone> getMilestones() { return milestones; }
    public void setMilestones(List<TimelineMilestone> milestones) { this.milestones = milestones; }

    public String getIcsCalendarContent() { return icsCalendarContent; }
    public void setIcsCalendarContent(String icsCalendarContent) { this.icsCalendarContent = icsCalendarContent; }

    public LocalDate getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDate generatedAt) { this.generatedAt = generatedAt; }
}
