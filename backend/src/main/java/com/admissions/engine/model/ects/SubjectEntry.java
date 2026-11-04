package com.admissions.engine.model.ects;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * An undergraduate subject completed by an applicant under AICTE / UGC Indian curriculum.
 */
public class SubjectEntry {

    private String id;
    private String name;
    private EctsCategory category;

    @JsonAlias({"credits", "indian_credits"})
    private double indianCredits;

    private int lectureHours = 3;
    private int tutorialHours = 0;
    private int practicalHours = 0;
    private String grade;
    private double calculatedEcts;
    private Integer semester;

    public SubjectEntry() {}

    public SubjectEntry(String id, String name, EctsCategory category, double indianCredits,
                        int lectureHours, int tutorialHours, int practicalHours,
                        String grade, double calculatedEcts, Integer semester) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.indianCredits = indianCredits;
        this.lectureHours = lectureHours;
        this.tutorialHours = tutorialHours;
        this.practicalHours = practicalHours;
        this.grade = grade;
        this.calculatedEcts = calculatedEcts;
        this.semester = semester;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public EctsCategory getCategory() { return category; }
    public void setCategory(EctsCategory category) { this.category = category; }

    public double getIndianCredits() { return indianCredits; }
    public void setIndianCredits(double indianCredits) { this.indianCredits = indianCredits; }

    public int getLectureHours() { return lectureHours; }
    public void setLectureHours(int lectureHours) { this.lectureHours = lectureHours; }

    public int getTutorialHours() { return tutorialHours; }
    public void setTutorialHours(int tutorialHours) { this.tutorialHours = tutorialHours; }

    public int getPracticalHours() { return practicalHours; }
    public void setPracticalHours(int practicalHours) { this.practicalHours = practicalHours; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public double getCalculatedEcts() { return calculatedEcts; }
    public void setCalculatedEcts(double calculatedEcts) { this.calculatedEcts = calculatedEcts; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }
}
