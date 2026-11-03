package com.admissions.engine.model.alignment;

import java.util.ArrayList;
import java.util.List;

/**
 * Output of the NLP/LLM Semantic Alignment Critic.
 */
public class AlignmentAnalysisResult {

    private double overallAlignmentScore;
    private String alignmentTier;
    private String targetUniversityName;
    private int wordCount;
    private List<String> detectedCandidateThemes = new ArrayList<>();
    private List<FacultyMatch> topFacultyMatches = new ArrayList<>();
    private List<String> overlappingKeywords = new ArrayList<>();
    private List<String> missingKeywords = new ArrayList<>();
    private List<String> actionableCritique = new ArrayList<>();
    private String naidakuGuidance;
    private ColdEmailDraft sampleColdEmail;

    public AlignmentAnalysisResult() {}

    public AlignmentAnalysisResult(double overallAlignmentScore, String alignmentTier,
                                   String targetUniversityName, int wordCount,
                                   List<String> detectedCandidateThemes, List<FacultyMatch> topFacultyMatches,
                                   List<String> overlappingKeywords, List<String> missingKeywords,
                                   List<String> actionableCritique, String naidakuGuidance,
                                   ColdEmailDraft sampleColdEmail) {
        this.overallAlignmentScore = overallAlignmentScore;
        this.alignmentTier = alignmentTier;
        this.targetUniversityName = targetUniversityName;
        this.wordCount = wordCount;
        this.detectedCandidateThemes = detectedCandidateThemes != null ? detectedCandidateThemes : new ArrayList<>();
        this.topFacultyMatches = topFacultyMatches != null ? topFacultyMatches : new ArrayList<>();
        this.overlappingKeywords = overlappingKeywords != null ? overlappingKeywords : new ArrayList<>();
        this.missingKeywords = missingKeywords != null ? missingKeywords : new ArrayList<>();
        this.actionableCritique = actionableCritique != null ? actionableCritique : new ArrayList<>();
        this.naidakuGuidance = naidakuGuidance;
        this.sampleColdEmail = sampleColdEmail;
    }

    public double getOverallAlignmentScore() { return overallAlignmentScore; }
    public void setOverallAlignmentScore(double overallAlignmentScore) { this.overallAlignmentScore = overallAlignmentScore; }

    public String getAlignmentTier() { return alignmentTier; }
    public void setAlignmentTier(String alignmentTier) { this.alignmentTier = alignmentTier; }

    public String getTargetUniversityName() { return targetUniversityName; }
    public void setTargetUniversityName(String targetUniversityName) { this.targetUniversityName = targetUniversityName; }

    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }

    public List<String> getDetectedCandidateThemes() { return detectedCandidateThemes; }
    public void setDetectedCandidateThemes(List<String> detectedCandidateThemes) { this.detectedCandidateThemes = detectedCandidateThemes; }

    public List<FacultyMatch> getTopFacultyMatches() { return topFacultyMatches; }
    public void setTopFacultyMatches(List<FacultyMatch> topFacultyMatches) { this.topFacultyMatches = topFacultyMatches; }

    public List<String> getOverlappingKeywords() { return overlappingKeywords; }
    public void setOverlappingKeywords(List<String> overlappingKeywords) { this.overlappingKeywords = overlappingKeywords; }

    public List<String> getMissingKeywords() { return missingKeywords; }
    public void setMissingKeywords(List<String> missingKeywords) { this.missingKeywords = missingKeywords; }

    public List<String> getActionableCritique() { return actionableCritique; }
    public void setActionableCritique(List<String> actionableCritique) { this.actionableCritique = actionableCritique; }

    public String getNaidakuGuidance() { return naidakuGuidance; }
    public void setNaidakuGuidance(String naidakuGuidance) { this.naidakuGuidance = naidakuGuidance; }

    public ColdEmailDraft getSampleColdEmail() { return sampleColdEmail; }
    public void setSampleColdEmail(ColdEmailDraft sampleColdEmail) { this.sampleColdEmail = sampleColdEmail; }
}
