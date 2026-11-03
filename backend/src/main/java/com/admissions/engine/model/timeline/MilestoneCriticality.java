package com.admissions.engine.model.timeline;

/**
 * Priority and critical path classification for an admissions or visa milestone.
 */
public enum MilestoneCriticality {
    CRITICAL_PATH("Critical Path — Zero Slack", "🔴"),
    HIGH_PRIORITY("High Priority", "🟠"),
    STANDARD("Standard Step", "🔵");

    private final String label;
    private final String badge;

    MilestoneCriticality(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }

    public String getLabel() {
        return label;
    }

    public String getBadge() {
        return badge;
    }
}
