package com.admissions.engine.model.alignment;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of evaluating alignment between an applicant's SOP/proposal and a faculty member's lab.
 */
public class FacultyMatch {

    private FacultyMember faculty;
    private double matchScore;
    private List<String> matchingKeywords = new ArrayList<>();
    private List<ResearchPaper> relevantPapers = new ArrayList<>();
    private String matchRationale;

    public FacultyMatch() {}

    public FacultyMatch(FacultyMember faculty, double matchScore, List<String> matchingKeywords,
                        List<ResearchPaper> relevantPapers, String matchRationale) {
        this.faculty = faculty;
        this.matchScore = matchScore;
        this.matchingKeywords = matchingKeywords != null ? matchingKeywords : new ArrayList<>();
        this.relevantPapers = relevantPapers != null ? relevantPapers : new ArrayList<>();
        this.matchRationale = matchRationale;
    }

    public FacultyMember getFaculty() { return faculty; }
    public void setFaculty(FacultyMember faculty) { this.faculty = faculty; }

    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }

    public List<String> getMatchingKeywords() { return matchingKeywords; }
    public void setMatchingKeywords(List<String> matchingKeywords) { this.matchingKeywords = matchingKeywords; }

    public List<ResearchPaper> getRelevantPapers() { return relevantPapers; }
    public void setRelevantPapers(List<ResearchPaper> relevantPapers) { this.relevantPapers = relevantPapers; }

    public String getMatchRationale() { return matchRationale; }
    public void setMatchRationale(String matchRationale) { this.matchRationale = matchRationale; }
}
