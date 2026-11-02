package com.admissions.engine.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.sql.Timestamp;

/**
 * JPA entity representing a graduate school applicant's complete academic profile.
 */
@Entity
@Table(name = "applicant_profiles")
public class ApplicantProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "applicant_id")
    private int applicantId;

    @NotNull(message = "Full name is required")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Email(message = "Must be a valid email address")
    @Column(name = "email", unique = true)
    private String email;

    @NotNull(message = "CGPA is required")
    @DecimalMin(value = "0.0", message = "CGPA must be >= 0")
    @DecimalMax(value = "10.0", message = "CGPA must be <= 10")
    @Column(name = "cgpa_10pt", nullable = false)
    @com.fasterxml.jackson.annotation.JsonAlias({"cgpa", "cgpa_10pt"})
    private double cgpa10pt;

    @Column(name = "cgpa_scale")
    private double cgpaScale = 10.0;

    @Column(name = "university_name")
    private String universityName;

    @Column(name = "degree")
    private String degree = "B.Tech";

    @Column(name = "specialization")
    private String specialization = "Computer Science";

    @Column(name = "graduation_year")
    private Integer graduationYear;

    @Min(value = 130, message = "GRE Verbal must be >= 130")
    @Max(value = 170, message = "GRE Verbal must be <= 170")
    @Column(name = "gre_verbal")
    private Integer greVerbal;

    @Min(value = 130, message = "GRE Quant must be >= 130")
    @Max(value = 170, message = "GRE Quant must be <= 170")
    @Column(name = "gre_quant")
    private Integer greQuant;

    @DecimalMin(value = "0.0", message = "GRE AW must be >= 0")
    @DecimalMax(value = "6.0", message = "GRE AW must be <= 6")
    @Column(name = "gre_aw")
    @com.fasterxml.jackson.annotation.JsonAlias({"greAW", "gre_aw"})
    private Double greAw;

    @Column(name = "gmat_score")
    @com.fasterxml.jackson.annotation.JsonAlias({"gmat", "gmat_score"})
    private Integer gmatScore;

    @Column(name = "toefl_score")
    @com.fasterxml.jackson.annotation.JsonAlias({"toefl", "toefl_score"})
    private Integer toeflScore;

    @Column(name = "ielts_score")
    @com.fasterxml.jackson.annotation.JsonAlias({"ielts", "ielts_score"})
    private Double ieltsScore;

    @Column(name = "jlpt_level")
    private String jlptLevel;

    @Column(name = "ects_credits_cs")
    @com.fasterxml.jackson.annotation.JsonAlias({"ectsCredits", "ects_credits_cs"})
    private Integer ectsCreditsCs;

    @Column(name = "thesis_completed")
    @com.fasterxml.jackson.annotation.JsonAlias({"bachelorThesis", "thesis_completed"})
    private Boolean thesisCompleted = false;

    @Lob
    @Column(name = "research_proposal", columnDefinition = "TEXT")
    private String researchProposal;

    @DecimalMin(value = "0.0", message = "Research score must be >= 0")
    @DecimalMax(value = "100.0", message = "Research score must be <= 100")
    @Column(name = "research_score")
    private Double researchScore;

    @Column(name = "sop_score")
    private Double sopScore;

    @Column(name = "publications_count")
    @com.fasterxml.jackson.annotation.JsonAlias({"publications", "publications_count"})
    private Integer publicationsCount = 0;

    @Column(name = "internship_months")
    private Integer internshipMonths = 0;

    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    public ApplicantProfile() {}

    public ApplicantProfile(int applicantId, String fullName, String email, double cgpa10pt, double cgpaScale,
                            String universityName, String degree, String specialization, Integer graduationYear,
                            Integer greVerbal, Integer greQuant, Double greAw, Integer gmatScore, Integer toeflScore,
                            Double ieltsScore, String jlptLevel, Integer ectsCreditsCs, Boolean thesisCompleted,
                            String researchProposal, Double researchScore, Double sopScore,
                            Integer publicationsCount, Integer internshipMonths, Timestamp createdAt, Timestamp updatedAt) {
        this.applicantId = applicantId;
        this.fullName = fullName;
        this.email = email;
        this.cgpa10pt = cgpa10pt;
        this.cgpaScale = cgpaScale;
        this.universityName = universityName;
        this.degree = degree != null ? degree : "B.Tech";
        this.specialization = specialization != null ? specialization : "Computer Science";
        this.graduationYear = graduationYear;
        this.greVerbal = greVerbal;
        this.greQuant = greQuant;
        this.greAw = greAw;
        this.gmatScore = gmatScore;
        this.toeflScore = toeflScore;
        this.ieltsScore = ieltsScore;
        this.jlptLevel = jlptLevel;
        this.ectsCreditsCs = ectsCreditsCs;
        this.thesisCompleted = thesisCompleted != null ? thesisCompleted : false;
        this.researchProposal = researchProposal;
        this.researchScore = researchScore;
        this.sopScore = sopScore;
        this.publicationsCount = publicationsCount != null ? publicationsCount : 0;
        this.internshipMonths = internshipMonths != null ? internshipMonths : 0;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ApplicantProfileBuilder builder() {
        return new ApplicantProfileBuilder();
    }

    public static class ApplicantProfileBuilder {
        private int applicantId;
        private String fullName;
        private String email;
        private double cgpa10pt;
        private double cgpaScale = 10.0;
        private String universityName;
        private String degree = "B.Tech";
        private String specialization = "Computer Science";
        private Integer graduationYear;
        private Integer greVerbal;
        private Integer greQuant;
        private Double greAw;
        private Integer gmatScore;
        private Integer toeflScore;
        private Double ieltsScore;
        private String jlptLevel;
        private Integer ectsCreditsCs;
        private Boolean thesisCompleted = false;
        private String researchProposal;
        private Double researchScore;
        private Double sopScore;
        private Integer publicationsCount = 0;
        private Integer internshipMonths = 0;
        private Timestamp createdAt;
        private Timestamp updatedAt;

        public ApplicantProfileBuilder applicantId(int applicantId) { this.applicantId = applicantId; return this; }
        public ApplicantProfileBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public ApplicantProfileBuilder email(String email) { this.email = email; return this; }
        public ApplicantProfileBuilder cgpa10pt(double cgpa10pt) { this.cgpa10pt = cgpa10pt; return this; }
        public ApplicantProfileBuilder cgpaScale(double cgpaScale) { this.cgpaScale = cgpaScale; return this; }
        public ApplicantProfileBuilder universityName(String universityName) { this.universityName = universityName; return this; }
        public ApplicantProfileBuilder degree(String degree) { this.degree = degree; return this; }
        public ApplicantProfileBuilder specialization(String specialization) { this.specialization = specialization; return this; }
        public ApplicantProfileBuilder graduationYear(Integer graduationYear) { this.graduationYear = graduationYear; return this; }
        public ApplicantProfileBuilder greVerbal(Integer greVerbal) { this.greVerbal = greVerbal; return this; }
        public ApplicantProfileBuilder greQuant(Integer greQuant) { this.greQuant = greQuant; return this; }
        public ApplicantProfileBuilder greAw(Double greAw) { this.greAw = greAw; return this; }
        public ApplicantProfileBuilder gmatScore(Integer gmatScore) { this.gmatScore = gmatScore; return this; }
        public ApplicantProfileBuilder toeflScore(Integer toeflScore) { this.toeflScore = toeflScore; return this; }
        public ApplicantProfileBuilder ieltsScore(Double ieltsScore) { this.ieltsScore = ieltsScore; return this; }
        public ApplicantProfileBuilder jlptLevel(String jlptLevel) { this.jlptLevel = jlptLevel; return this; }
        public ApplicantProfileBuilder ectsCreditsCs(Integer ectsCreditsCs) { this.ectsCreditsCs = ectsCreditsCs; return this; }
        public ApplicantProfileBuilder thesisCompleted(Boolean thesisCompleted) { this.thesisCompleted = thesisCompleted; return this; }
        public ApplicantProfileBuilder researchProposal(String researchProposal) { this.researchProposal = researchProposal; return this; }
        public ApplicantProfileBuilder researchScore(Double researchScore) { this.researchScore = researchScore; return this; }
        public ApplicantProfileBuilder sopScore(Double sopScore) { this.sopScore = sopScore; return this; }
        public ApplicantProfileBuilder publicationsCount(Integer publicationsCount) { this.publicationsCount = publicationsCount; return this; }
        public ApplicantProfileBuilder internshipMonths(Integer internshipMonths) { this.internshipMonths = internshipMonths; return this; }
        public ApplicantProfileBuilder createdAt(Timestamp createdAt) { this.createdAt = createdAt; return this; }
        public ApplicantProfileBuilder updatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; return this; }

        public ApplicantProfile build() {
            return new ApplicantProfile(applicantId, fullName, email, cgpa10pt, cgpaScale, universityName,
                    degree, specialization, graduationYear, greVerbal, greQuant, greAw, gmatScore,
                    toeflScore, ieltsScore, jlptLevel, ectsCreditsCs, thesisCompleted, researchProposal,
                    researchScore, sopScore, publicationsCount, internshipMonths, createdAt, updatedAt);
        }
    }

    @PrePersist
    protected void onPrePersist() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onPreUpdate() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public int getApplicantId() { return applicantId; }
    public void setApplicantId(int applicantId) { this.applicantId = applicantId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getCgpa10pt() { return cgpa10pt; }
    public void setCgpa10pt(double cgpa10pt) { this.cgpa10pt = cgpa10pt; }

    public double getCgpaScale() { return cgpaScale; }
    public void setCgpaScale(double cgpaScale) { this.cgpaScale = cgpaScale; }

    public String getUniversityName() { return universityName; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }

    public Integer getGreVerbal() { return greVerbal; }
    public void setGreVerbal(Integer greVerbal) { this.greVerbal = greVerbal; }

    public Integer getGreQuant() { return greQuant; }
    public void setGreQuant(Integer greQuant) { this.greQuant = greQuant; }

    public Double getGreAw() { return greAw; }
    public void setGreAw(Double greAw) { this.greAw = greAw; }

    public Integer getGmatScore() { return gmatScore; }
    public void setGmatScore(Integer gmatScore) { this.gmatScore = gmatScore; }

    public Integer getToeflScore() { return toeflScore; }
    public void setToeflScore(Integer toeflScore) { this.toeflScore = toeflScore; }

    public Double getIeltsScore() { return ieltsScore; }
    public void setIeltsScore(Double ieltsScore) { this.ieltsScore = ieltsScore; }

    public String getJlptLevel() { return jlptLevel; }
    public void setJlptLevel(String jlptLevel) { this.jlptLevel = jlptLevel; }

    public Integer getEctsCreditsCs() { return ectsCreditsCs; }
    public void setEctsCreditsCs(Integer ectsCreditsCs) { this.ectsCreditsCs = ectsCreditsCs; }

    public Boolean getThesisCompleted() { return thesisCompleted; }
    public void setThesisCompleted(Boolean thesisCompleted) { this.thesisCompleted = thesisCompleted; }

    public String getResearchProposal() { return researchProposal; }
    public void setResearchProposal(String researchProposal) { this.researchProposal = researchProposal; }

    public Double getResearchScore() { return researchScore; }
    public void setResearchScore(Double researchScore) { this.researchScore = researchScore; }

    public Double getSopScore() { return sopScore; }
    public void setSopScore(Double sopScore) { this.sopScore = sopScore; }

    public Integer getPublicationsCount() { return publicationsCount; }
    public void setPublicationsCount(Integer publicationsCount) { this.publicationsCount = publicationsCount; }

    public Integer getInternshipMonths() { return internshipMonths; }
    public void setInternshipMonths(Integer internshipMonths) { this.internshipMonths = internshipMonths; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
