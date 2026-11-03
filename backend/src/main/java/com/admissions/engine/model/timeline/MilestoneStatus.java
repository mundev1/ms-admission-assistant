package com.admissions.engine.model.timeline;

/**
 * Real-time operational urgency of a milestone relative to the current date.
 */
public enum MilestoneStatus {
    OVERDUE("Overdue — Immediate Action Required", "⚠️"),
    URGENT("Urgent — Due Within 30 Days", "⏳"),
    UPCOMING("Upcoming — On Schedule", "📅"),
    COMPLETED("Completed", "✅");

    private final String description;
    private final String icon;

    MilestoneStatus(String description, String icon) {
        this.description = description;
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }
}
