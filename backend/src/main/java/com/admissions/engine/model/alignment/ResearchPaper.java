package com.admissions.engine.model.alignment;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a recent research paper published by a faculty laboratory,
 * indexed from arXiv, IEEE, ACM, or top-tier conferences (NeurIPS, ICML, CVPR, ACL).
 */
public class ResearchPaper {

    private String title;
    private String venue;
    private int year;
    private String url;
    private String abstractSnippet;
    private List<String> keywords = new ArrayList<>();

    public ResearchPaper() {}

    public ResearchPaper(String title, String venue, int year, String url, String abstractSnippet, List<String> keywords) {
        this.title = title;
        this.venue = venue;
        this.year = year;
        this.url = url;
        this.abstractSnippet = abstractSnippet;
        this.keywords = keywords != null ? keywords : new ArrayList<>();
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getAbstractSnippet() { return abstractSnippet; }
    public void setAbstractSnippet(String abstractSnippet) { this.abstractSnippet = abstractSnippet; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
}
