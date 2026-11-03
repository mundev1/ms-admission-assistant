package com.admissions.engine.model.timeline;

/**
 * High-level stages in the master's admissions and visa lifecycle.
 */
public enum TimelinePhase {
    TESTS_AND_PREP("Standardized Tests & Profile Prep", "📝"),
    CREDENTIAL_VERIFICATION("Credential Verification & Attestation", "📜"),
    UNIVERSITY_APPLICATIONS("University Applications & SOPs", "🏛️"),
    FINANCES_AND_ADMISSION("Admissions & Financial Proof", "💰"),
    VISA_AND_EMBASSY("Visa Slot & Consular Processing", "🛂"),
    PRE_DEPARTURE("Pre-Departure & Relocation", "✈️");

    private final String displayName;
    private final String emoji;

    TimelinePhase(String displayName, String emoji) {
        this.displayName = displayName;
        this.emoji = emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }
}
