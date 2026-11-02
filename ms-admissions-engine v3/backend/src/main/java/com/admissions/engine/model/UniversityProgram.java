package com.admissions.engine.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

/**
 * JPA entity representing a graduate program offered by a university.
 */
@Entity
@Table(name = "university_programs")
public class UniversityProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "program_id")
    private int programId;

    @Column(name = "university_name")
    private String universityName;

    @Column(name = "program_name")
    private String programName;

    @Column(name = "country")
    private String country;

    @Column(name = "degree_type")
    private String degreeType;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "framework_id")
    private int frameworkId;

    @Column(name = "region")
    private String region;

    @Column(name = "threshold_gpa")
    private Double thresholdGpa;

    @Column(name = "threshold_gre_v")
    private Integer thresholdGreV;

    @Column(name = "threshold_gre_q")
    private Integer thresholdGreQ;

    @Column(name = "threshold_gre_aw")
    private Double thresholdGreAw;

    @Column(name = "threshold_jlpt")
    private String thresholdJlpt;

    @Column(name = "threshold_ielts")
    private Double thresholdIelts;

    @Column(name = "threshold_toefl")
    private Integer thresholdToefl;

    @Column(name = "threshold_ects_cs")
    private Integer thresholdEctsCs;

    @Column(name = "thesis_required")
    private Boolean thesisRequired;

    @Column(name = "acceptance_rate")
    private Double acceptanceRate;

    @Column(name = "avg_accepted_gpa")
    private Double avgAcceptedGpa;

    @Column(name = "scrape_source_url")
    private String scrapeSourceUrl;

    @Column(name = "last_scraped_at")
    private Timestamp lastScrapedAt;

    @Transient
    private ScoringWeights weights;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "framework_id", insertable = false, updatable = false)
    private CountryFramework countryFramework;

    public UniversityProgram() {}

    public UniversityProgram(int programId, String universityName, String programName, String country,
                             String degreeType, String specialization, int frameworkId, String region,
                             Double thresholdGpa, Integer thresholdGreV, Integer thresholdGreQ,
                             Double thresholdGreAw, String thresholdJlpt, Double thresholdIelts,
                             Integer thresholdToefl, Integer thresholdEctsCs, Boolean thesisRequired,
                             Double acceptanceRate, Double avgAcceptedGpa, String scrapeSourceUrl,
                             Timestamp lastScrapedAt, ScoringWeights weights, CountryFramework countryFramework) {
        this.programId = programId;
        this.universityName = universityName;
        this.programName = programName;
        this.country = country;
        this.degreeType = degreeType;
        this.specialization = specialization;
        this.frameworkId = frameworkId;
        this.region = region;
        this.thresholdGpa = thresholdGpa;
        this.thresholdGreV = thresholdGreV;
        this.thresholdGreQ = thresholdGreQ;
        this.thresholdGreAw = thresholdGreAw;
        this.thresholdJlpt = thresholdJlpt;
        this.thresholdIelts = thresholdIelts;
        this.thresholdToefl = thresholdToefl;
        this.thresholdEctsCs = thresholdEctsCs;
        this.thesisRequired = thesisRequired;
        this.acceptanceRate = acceptanceRate;
        this.avgAcceptedGpa = avgAcceptedGpa;
        this.scrapeSourceUrl = scrapeSourceUrl;
        this.lastScrapedAt = lastScrapedAt;
        this.weights = weights;
        this.countryFramework = countryFramework;
    }

    public static UniversityProgramBuilder builder() {
        return new UniversityProgramBuilder();
    }

    public static class UniversityProgramBuilder {
        private int programId;
        private String universityName;
        private String programName;
        private String country;
        private String degreeType;
        private String specialization;
        private int frameworkId;
        private String region;
        private Double thresholdGpa;
        private Integer thresholdGreV;
        private Integer thresholdGreQ;
        private Double thresholdGreAw;
        private String thresholdJlpt;
        private Double thresholdIelts;
        private Integer thresholdToefl;
        private Integer thresholdEctsCs;
        private Boolean thesisRequired;
        private Double acceptanceRate;
        private Double avgAcceptedGpa;
        private String scrapeSourceUrl;
        private Timestamp lastScrapedAt;
        private ScoringWeights weights;
        private CountryFramework countryFramework;

        public UniversityProgramBuilder programId(int programId) { this.programId = programId; return this; }
        public UniversityProgramBuilder universityName(String universityName) { this.universityName = universityName; return this; }
        public UniversityProgramBuilder programName(String programName) { this.programName = programName; return this; }
        public UniversityProgramBuilder country(String country) { this.country = country; return this; }
        public UniversityProgramBuilder degreeType(String degreeType) { this.degreeType = degreeType; return this; }
        public UniversityProgramBuilder specialization(String specialization) { this.specialization = specialization; return this; }
        public UniversityProgramBuilder frameworkId(int frameworkId) { this.frameworkId = frameworkId; return this; }
        public UniversityProgramBuilder region(String region) { this.region = region; return this; }
        public UniversityProgramBuilder thresholdGpa(Double thresholdGpa) { this.thresholdGpa = thresholdGpa; return this; }
        public UniversityProgramBuilder thresholdGreV(Integer thresholdGreV) { this.thresholdGreV = thresholdGreV; return this; }
        public UniversityProgramBuilder thresholdGreQ(Integer thresholdGreQ) { this.thresholdGreQ = thresholdGreQ; return this; }
        public UniversityProgramBuilder thresholdGreAw(Double thresholdGreAw) { this.thresholdGreAw = thresholdGreAw; return this; }
        public UniversityProgramBuilder thresholdJlpt(String thresholdJlpt) { this.thresholdJlpt = thresholdJlpt; return this; }
        public UniversityProgramBuilder thresholdIelts(Double thresholdIelts) { this.thresholdIelts = thresholdIelts; return this; }
        public UniversityProgramBuilder thresholdToefl(Integer thresholdToefl) { this.thresholdToefl = thresholdToefl; return this; }
        public UniversityProgramBuilder thresholdEctsCs(Integer thresholdEctsCs) { this.thresholdEctsCs = thresholdEctsCs; return this; }
        public UniversityProgramBuilder thesisRequired(Boolean thesisRequired) { this.thesisRequired = thesisRequired; return this; }
        public UniversityProgramBuilder acceptanceRate(Double acceptanceRate) { this.acceptanceRate = acceptanceRate; return this; }
        public UniversityProgramBuilder avgAcceptedGpa(Double avgAcceptedGpa) { this.avgAcceptedGpa = avgAcceptedGpa; return this; }
        public UniversityProgramBuilder scrapeSourceUrl(String scrapeSourceUrl) { this.scrapeSourceUrl = scrapeSourceUrl; return this; }
        public UniversityProgramBuilder lastScrapedAt(Timestamp lastScrapedAt) { this.lastScrapedAt = lastScrapedAt; return this; }
        public UniversityProgramBuilder weights(ScoringWeights weights) { this.weights = weights; return this; }
        public UniversityProgramBuilder countryFramework(CountryFramework countryFramework) { this.countryFramework = countryFramework; return this; }

        public UniversityProgram build() {
            return new UniversityProgram(programId, universityName, programName, country, degreeType,
                    specialization, frameworkId, region, thresholdGpa, thresholdGreV, thresholdGreQ,
                    thresholdGreAw, thresholdJlpt, thresholdIelts, thresholdToefl, thresholdEctsCs,
                    thesisRequired, acceptanceRate, avgAcceptedGpa, scrapeSourceUrl, lastScrapedAt,
                    weights, countryFramework);
        }
    }

    public int getProgramId() { return programId; }
    public void setProgramId(int programId) { this.programId = programId; }

    public String getUniversityName() { return universityName; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }

    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getDegreeType() { return degreeType; }
    public void setDegreeType(String degreeType) { this.degreeType = degreeType; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public int getFrameworkId() { return frameworkId; }
    public void setFrameworkId(int frameworkId) { this.frameworkId = frameworkId; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Double getThresholdGpa() { return thresholdGpa; }
    public void setThresholdGpa(Double thresholdGpa) { this.thresholdGpa = thresholdGpa; }

    public Integer getThresholdGreV() { return thresholdGreV; }
    public void setThresholdGreV(Integer thresholdGreV) { this.thresholdGreV = thresholdGreV; }

    public Integer getThresholdGreQ() { return thresholdGreQ; }
    public void setThresholdGreQ(Integer thresholdGreQ) { this.thresholdGreQ = thresholdGreQ; }

    public Double getThresholdGreAw() { return thresholdGreAw; }
    public void setThresholdGreAw(Double thresholdGreAw) { this.thresholdGreAw = thresholdGreAw; }

    public String getThresholdJlpt() { return thresholdJlpt; }
    public void setThresholdJlpt(String thresholdJlpt) { this.thresholdJlpt = thresholdJlpt; }

    public Double getThresholdIelts() { return thresholdIelts; }
    public void setThresholdIelts(Double thresholdIelts) { this.thresholdIelts = thresholdIelts; }

    public Integer getThresholdToefl() { return thresholdToefl; }
    public void setThresholdToefl(Integer thresholdToefl) { this.thresholdToefl = thresholdToefl; }

    public Integer getThresholdEctsCs() { return thresholdEctsCs; }
    public void setThresholdEctsCs(Integer thresholdEctsCs) { this.thresholdEctsCs = thresholdEctsCs; }

    public Boolean getThesisRequired() { return thesisRequired; }
    public void setThesisRequired(Boolean thesisRequired) { this.thesisRequired = thesisRequired; }

    public Double getAcceptanceRate() { return acceptanceRate; }
    public void setAcceptanceRate(Double acceptanceRate) { this.acceptanceRate = acceptanceRate; }

    public Double getAvgAcceptedGpa() { return avgAcceptedGpa; }
    public void setAvgAcceptedGpa(Double avgAcceptedGpa) { this.avgAcceptedGpa = avgAcceptedGpa; }

    public String getScrapeSourceUrl() { return scrapeSourceUrl; }
    public void setScrapeSourceUrl(String scrapeSourceUrl) { this.scrapeSourceUrl = scrapeSourceUrl; }

    public Timestamp getLastScrapedAt() { return lastScrapedAt; }
    public void setLastScrapedAt(Timestamp lastScrapedAt) { this.lastScrapedAt = lastScrapedAt; }

    public ScoringWeights getWeights() { return weights; }
    public void setWeights(ScoringWeights weights) { this.weights = weights; }

    public CountryFramework getCountryFramework() { return countryFramework; }
    public void setCountryFramework(CountryFramework countryFramework) { this.countryFramework = countryFramework; }
}
