package com.admissions.engine.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Plain value object describing the gap between an applicant's credentials
 * and a specific program's admission thresholds.
 *
 * <p>Populated by {@link com.admissions.engine.calculator.DeficitAnalyzer} and
 * bundled into a {@link ScoreResult} for client consumption.</p>
 */
public class DeficitReport {

    /**
     * Map of field name → human-readable delta description.
     * Example: {@code {"greQuant" -> "Deficit of 5 points (160 required, 155 provided)"}}
     */
    private Map<String, String> fieldDeltas;

    /**
     * Ordered list of concrete improvement actions the applicant can take.
     * Example: {@code "Retake GRE Quantitative to score at least 160."}
     */
    private List<String> actionableRecommendations;

    /**
     * {@code true} if the applicant meets every threshold for the target program,
     * {@code false} if at least one deficit exists.
     */
    private boolean isMeetingAllThresholds;

    /**
     * Default constructor initialising empty collections.
     */
    public DeficitReport() {
        this.fieldDeltas = new HashMap<>();
        this.actionableRecommendations = new ArrayList<>();
        this.isMeetingAllThresholds = true;
    }

    /**
     * Full constructor.
     *
     * @param fieldDeltas               map of field-level deficit descriptions
     * @param actionableRecommendations ordered list of improvement actions
     * @param isMeetingAllThresholds    whether all thresholds are met
     */
    public DeficitReport(Map<String, String> fieldDeltas,
                         List<String> actionableRecommendations,
                         boolean isMeetingAllThresholds) {
        this.fieldDeltas = fieldDeltas != null ? fieldDeltas : new HashMap<>();
        this.actionableRecommendations = actionableRecommendations != null
                ? actionableRecommendations : new ArrayList<>();
        this.isMeetingAllThresholds = isMeetingAllThresholds;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    /**
     * Returns the map of field-level deficit descriptions.
     *
     * @return field deltas map; never {@code null}
     */
    public Map<String, String> getFieldDeltas() {
        return fieldDeltas;
    }

    /**
     * Sets the field-level deficit map.
     *
     * @param fieldDeltas non-null map to assign
     */
    public void setFieldDeltas(Map<String, String> fieldDeltas) {
        this.fieldDeltas = fieldDeltas;
    }

    /**
     * Returns actionable improvement recommendations.
     *
     * @return list of recommendation strings; never {@code null}
     */
    public List<String> getActionableRecommendations() {
        return actionableRecommendations;
    }

    /**
     * Sets actionable recommendations.
     *
     * @param actionableRecommendations non-null list to assign
     */
    public void setActionableRecommendations(List<String> actionableRecommendations) {
        this.actionableRecommendations = actionableRecommendations;
    }

    /**
     * Returns whether the applicant meets every threshold for the target program.
     *
     * @return {@code true} if all thresholds are satisfied
     */
    public boolean isMeetingAllThresholds() {
        return isMeetingAllThresholds;
    }

    /**
     * Sets whether all thresholds are met.
     *
     * @param meetingAllThresholds the updated flag value
     */
    public void setMeetingAllThresholds(boolean meetingAllThresholds) {
        this.isMeetingAllThresholds = meetingAllThresholds;
    }

    // ── Business Methods ─────────────────────────────────────────────────────

    /**
     * Generates a multi-line human-readable summary of all deficits and recommendations.
     *
     * @return formatted deficit summary string; or a positive confirmation if no deficits found
     */
    public String getSummary() {
        if (isMeetingAllThresholds || fieldDeltas.isEmpty()) {
            return "✅ Applicant meets all thresholds for this program. No deficits detected.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("⚠️  DEFICIT REPORT\n");
        sb.append("─".repeat(50)).append("\n");
        sb.append("Fields with deficits:\n");

        fieldDeltas.forEach((field, delta) ->
                sb.append("  • ").append(field).append(": ").append(delta).append("\n"));

        if (!actionableRecommendations.isEmpty()) {
            sb.append("\n📋 Recommended Actions:\n");
            for (int i = 0; i < actionableRecommendations.size(); i++) {
                sb.append("  ").append(i + 1).append(". ")
                  .append(actionableRecommendations.get(i)).append("\n");
            }
        }

        sb.append("─".repeat(50)).append("\n");
        sb.append("Total deficits: ").append(fieldDeltas.size());
        return sb.toString();
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
