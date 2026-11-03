package com.admissions.engine.model.alignment;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a university faculty member / PI leading an active research laboratory.
 */
public class FacultyMember {

    private String id;
    private String name;
    private String title;
    private String universityName;
    private String department;
    private String labName;
    private String labWebsite;
    private String country;
    private String email;
    private List<String> researchThemes = new ArrayList<>();
    private boolean acceptingStudents;
    private boolean naidakuRequired;
    private List<ResearchPaper> recentPublications = new ArrayList<>();
    private String contactAdvice;

    public FacultyMember() {}

    public FacultyMember(String id, String name, String title, String universityName,
                         String department, String labName, String labWebsite, String country,
                         String email, List<String> researchThemes, boolean acceptingStudents,
                         boolean naidakuRequired, List<ResearchPaper> recentPublications,
                         String contactAdvice) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.universityName = universityName;
        this.department = department;
        this.labName = labName;
        this.labWebsite = labWebsite;
        this.country = country;
        this.email = email;
        this.researchThemes = researchThemes != null ? researchThemes : new ArrayList<>();
        this.acceptingStudents = acceptingStudents;
        this.naidakuRequired = naidakuRequired;
        this.recentPublications = recentPublications != null ? recentPublications : new ArrayList<>();
        this.contactAdvice = contactAdvice;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUniversityName() { return universityName; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getLabName() { return labName; }
    public void setLabName(String labName) { this.labName = labName; }

    public String getLabWebsite() { return labWebsite; }
    public void setLabWebsite(String labWebsite) { this.labWebsite = labWebsite; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getResearchThemes() { return researchThemes; }
    public void setResearchThemes(List<String> researchThemes) { this.researchThemes = researchThemes; }

    public boolean isAcceptingStudents() { return acceptingStudents; }
    public void setAcceptingStudents(boolean acceptingStudents) { this.acceptingStudents = acceptingStudents; }

    public boolean isNaidakuRequired() { return naidakuRequired; }
    public void setNaidakuRequired(boolean naidakuRequired) { this.naidakuRequired = naidakuRequired; }

    public List<ResearchPaper> getRecentPublications() { return recentPublications; }
    public void setRecentPublications(List<ResearchPaper> recentPublications) { this.recentPublications = recentPublications; }

    public String getContactAdvice() { return contactAdvice; }
    public void setContactAdvice(String contactAdvice) { this.contactAdvice = contactAdvice; }
}
