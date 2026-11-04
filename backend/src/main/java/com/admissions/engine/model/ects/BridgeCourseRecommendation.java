package com.admissions.engine.model.ects;

/**
 * Recommended remedial course, MOOC, or university bridge exam to bridge prerequisite ECTS deficits.
 */
public class BridgeCourseRecommendation {

    private String id;
    private EctsCategory category;
    private String title;
    private String platform;
    private double estimatedEcts;
    private String duration;
    private String url;
    private String recognitionNotes;
    private String syllabusCoverage;

    public BridgeCourseRecommendation() {}

    public BridgeCourseRecommendation(String id, EctsCategory category, String title,
                                      String platform, double estimatedEcts, String duration,
                                      String url, String recognitionNotes, String syllabusCoverage) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.platform = platform;
        this.estimatedEcts = estimatedEcts;
        this.duration = duration;
        this.url = url;
        this.recognitionNotes = recognitionNotes;
        this.syllabusCoverage = syllabusCoverage;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public EctsCategory getCategory() { return category; }
    public void setCategory(EctsCategory category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public double getEstimatedEcts() { return estimatedEcts; }
    public void setEstimatedEcts(double estimatedEcts) { this.estimatedEcts = estimatedEcts; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getRecognitionNotes() { return recognitionNotes; }
    public void setRecognitionNotes(String recognitionNotes) { this.recognitionNotes = recognitionNotes; }

    public String getSyllabusCoverage() { return syllabusCoverage; }
    public void setSyllabusCoverage(String syllabusCoverage) { this.syllabusCoverage = syllabusCoverage; }
}
