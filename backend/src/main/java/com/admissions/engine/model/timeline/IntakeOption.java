package com.admissions.engine.model.timeline;

import java.time.LocalDate;

/**
 * Standard university admission intake cycle.
 */
public class IntakeOption {

    private String id;
    private String name;
    private String region;
    private String country;
    private String season;
    private int targetYear;
    private LocalDate defaultStartDate;
    private String description;

    public IntakeOption() {}

    public IntakeOption(String id, String name, String region, String country,
                        String season, int targetYear, LocalDate defaultStartDate, String description) {
        this.id = id;
        this.name = name;
        this.region = region;
        this.country = country;
        this.season = season;
        this.targetYear = targetYear;
        this.defaultStartDate = defaultStartDate;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public int getTargetYear() { return targetYear; }
    public void setTargetYear(int targetYear) { this.targetYear = targetYear; }

    public LocalDate getDefaultStartDate() { return defaultStartDate; }
    public void setDefaultStartDate(LocalDate defaultStartDate) { this.defaultStartDate = defaultStartDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
