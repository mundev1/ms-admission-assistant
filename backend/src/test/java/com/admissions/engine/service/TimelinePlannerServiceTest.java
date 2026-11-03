package com.admissions.engine.service;

import com.admissions.engine.model.timeline.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TimelinePlannerService Backwards Scheduling Tests")
class TimelinePlannerServiceTest {

    private TimelinePlannerService timelineService;

    @BeforeEach
    void setUp() {
        timelineService = new TimelinePlannerService();
    }

    @Test
    @DisplayName("getIntakeOptions returns comprehensive intake cycles")
    void testGetIntakeOptions() {
        List<IntakeOption> options = timelineService.getIntakeOptions();
        assertNotNull(options);
        assertTrue(options.size() >= 4);

        boolean hasFall2027 = options.stream().anyMatch(o -> "FALL_2027".equals(o.getId()));
        boolean hasJapanApril = options.stream().anyMatch(o -> "JAPAN_APRIL_2027".equals(o.getId()));
        boolean hasJapanOct = options.stream().anyMatch(o -> "JAPAN_OCTOBER_2027".equals(o.getId()));

        assertTrue(hasFall2027, "Must contain Fall 2027 intake");
        assertTrue(hasJapanApril, "Must contain Japan April intake");
        assertTrue(hasJapanOct, "Must contain Japan October intake");
    }

    @Test
    @DisplayName("Germany Fall 2027 includes APS certificate on critical path 300 days prior")
    void testGermanyFall2027Timeline() {
        TimelineRequest req = new TimelineRequest();
        req.setIntakeId("FALL_2027");
        req.setTargetCountry("Germany");
        req.setTargetStartDate(LocalDate.of(2027, 9, 1));

        TimelineSchedule schedule = timelineService.generateTimeline(req);

        assertNotNull(schedule);
        assertEquals("Germany", schedule.getCountry());
        assertEquals(LocalDate.of(2027, 9, 1), schedule.getTargetStartDate());
        assertTrue(schedule.getMilestones().size() >= 7);
        assertTrue(schedule.getCriticalPathCount() >= 3);

        // Check for APS certificate milestone
        TimelineMilestone apsMilestone = schedule.getMilestones().stream()
                .filter(m -> m.getTitle().contains("APS Certificate"))
                .findFirst()
                .orElse(null);

        assertNotNull(apsMilestone, "Must include German APS certificate milestone");
        assertEquals(MilestoneCriticality.CRITICAL_PATH, apsMilestone.getCriticality());
        assertEquals(TimelinePhase.CREDENTIAL_VERIFICATION, apsMilestone.getPhase());
        assertTrue(apsMilestone.getIndianStudentAdvisory().contains("APS India takes 60–120 days"));

        // Verify target date is approx 300 days before Sept 1, 2027 -> Nov 5, 2026
        assertEquals(LocalDate.of(2027, 9, 1).minusDays(300), apsMilestone.getTargetDate());

        // Check for Sperrkonto blocked account
        boolean hasBlockedAccount = schedule.getMilestones().stream()
                .anyMatch(m -> m.getTitle().contains("Sperrkonto") || m.getTitle().contains("Blocked Account"));
        assertTrue(hasBlockedAccount, "Must include Sperrkonto blocked account milestone");

        // Check for VFS Waitlist alert in critical alerts
        boolean hasApsAlert = schedule.getCriticalAlerts().stream()
                .anyMatch(a -> a.contains("APS Certificate Backlog"));
        assertTrue(hasApsAlert, "Must contain German APS advisory alert");
    }

    @Test
    @DisplayName("USA Fall 2027 includes GRE, WES, I-20, and F-1 visa interview")
    void testUsFall2027Timeline() {
        TimelineRequest req = new TimelineRequest();
        req.setIntakeId("FALL_2027");
        req.setTargetCountry("USA");
        req.setTargetStartDate(LocalDate.of(2027, 8, 25));

        TimelineSchedule schedule = timelineService.generateTimeline(req);

        assertNotNull(schedule);
        assertEquals("USA", schedule.getCountry());

        // Check for WES credential evaluation
        boolean hasWes = schedule.getMilestones().stream()
                .anyMatch(m -> m.getTitle().contains("WES"));
        assertTrue(hasWes, "Must include WES credential evaluation for USA");

        // Check for I-20 issuance
        boolean hasI20 = schedule.getMilestones().stream()
                .anyMatch(m -> m.getTitle().contains("I-20"));
        assertTrue(hasI20, "Must include Form I-20 issuance for USA");

        // Check for F-1 visa slot booking
        boolean hasF1 = schedule.getMilestones().stream()
                .anyMatch(m -> m.getTitle().contains("F-1"));
        assertTrue(hasF1, "Must include F-1 visa appointment milestone");

        // Verify SEVIS 30-day legal entry window
        boolean hasPortOfEntry = schedule.getMilestones().stream()
                .anyMatch(m -> m.getTitle().contains("Port of Entry"));
        assertTrue(hasPortOfEntry, "Must include 30-day legal Port of Entry milestone");
    }

    @Test
    @DisplayName("Japan Spring 2027 aligns JLPT to July of previous year and includes Naidaku + COE")
    void testJapanSpring2027Timeline() {
        TimelineRequest req = new TimelineRequest();
        req.setIntakeId("JAPAN_APRIL_2027");
        req.setTargetCountry("Japan");
        req.setTargetStartDate(LocalDate.of(2027, 4, 1));

        TimelineSchedule schedule = timelineService.generateTimeline(req);

        assertNotNull(schedule);
        assertEquals("Japan", schedule.getCountry());

        // Check JLPT milestone date
        TimelineMilestone jlpt = schedule.getMilestones().stream()
                .filter(m -> m.getTitle().contains("JLPT"))
                .findFirst()
                .orElse(null);

        assertNotNull(jlpt, "Must contain JLPT milestone");
        assertEquals(MilestoneCriticality.CRITICAL_PATH, jlpt.getCriticality());
        // For April 2027, the JLPT must be July 2026 (first Sunday of July 2026 = July 5, 2026)
        assertEquals(7, jlpt.getTargetDate().getMonthValue(), "Must be July exam");
        assertEquals(2026, jlpt.getTargetDate().getYear(), "Must be July of the preceding year");

        // Check Naidaku (professor approval)
        boolean hasNaidaku = schedule.getMilestones().stream()
                .anyMatch(m -> m.getTitle().contains("Naidaku") || m.getTitle().contains("Lab Supervisor"));
        assertTrue(hasNaidaku, "Must include Professor prior approval Naidaku");

        // Check Certificate of Eligibility (COE)
        boolean hasCoe = schedule.getMilestones().stream()
                .anyMatch(m -> m.getTitle().contains("Certificate of Eligibility"));
        assertTrue(hasCoe, "Must include Certificate of Eligibility (COE) filing");
    }

    @Test
    @DisplayName("RFC 5545 iCalendar content generates valid VCALENDAR with VEVENT and VALARM")
    void testIcsCalendarGeneration() {
        TimelineRequest req = new TimelineRequest();
        req.setIntakeId("FALL_2027");
        req.setTargetCountry("Germany");
        req.setTargetStartDate(LocalDate.of(2027, 9, 1));

        TimelineSchedule schedule = timelineService.generateTimeline(req);
        String ics = schedule.getIcsCalendarContent();

        assertNotNull(ics);
        assertTrue(ics.startsWith("BEGIN:VCALENDAR"), "ICS must start with BEGIN:VCALENDAR");
        assertTrue(ics.endsWith("END:VCALENDAR\r\n"), "ICS must end with END:VCALENDAR");
        assertTrue(ics.contains("BEGIN:VEVENT"), "Must contain events");
        assertTrue(ics.contains("BEGIN:VALARM"), "Must contain reminder alarms");
        assertTrue(ics.contains("TRIGGER:-P7D"), "Must contain 7-day prior reminder alarm");
        assertTrue(ics.contains("German APS Certificate Application"), "Must contain event title");
    }
}
