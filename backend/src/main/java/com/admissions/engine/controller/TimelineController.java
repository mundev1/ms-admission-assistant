package com.admissions.engine.controller;

import com.admissions.engine.model.timeline.IntakeOption;
import com.admissions.engine.model.timeline.TimelineRequest;
import com.admissions.engine.model.timeline.TimelineSchedule;
import com.admissions.engine.service.TimelinePlannerService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * REST controller for Admissions Intake & Visa Deadline Timeline Planner.
 *
 * <p>Base path: {@code /api/timeline}</p>
 */
@RestController
@RequestMapping("/api/timeline")
@CrossOrigin(originPatterns = "*")
public class TimelineController {

    private final TimelinePlannerService timelinePlannerService;

    public TimelineController(TimelinePlannerService timelinePlannerService) {
        this.timelinePlannerService = timelinePlannerService;
    }

    /**
     * Retrieves all standard admissions intake cycles with default target dates.
     *
     * @return list of intake options
     */
    @GetMapping("/intakes")
    public ResponseEntity<List<IntakeOption>> getIntakes() {
        return ResponseEntity.ok(timelinePlannerService.getIntakeOptions());
    }

    /**
     * Calculates backwards-scheduled admissions deadlines and visa milestones.
     *
     * @param request target intake, country, and start date
     * @return complete calculated timeline schedule
     */
    @PostMapping("/plan")
    public ResponseEntity<TimelineSchedule> planTimeline(@RequestBody TimelineRequest request) {
        TimelineSchedule schedule = timelinePlannerService.generateTimeline(request);
        return ResponseEntity.ok(schedule);
    }

    /**
     * Generates and downloads an RFC 5545 compliant .ics calendar file.
     *
     * @param request target intake, country, and start date
     * @return downloadable iCalendar file
     */
    @PostMapping("/export/ics")
    public ResponseEntity<byte[]> exportIcsCalendar(@RequestBody TimelineRequest request) {
        TimelineSchedule schedule = timelinePlannerService.generateTimeline(request);
        String icsContent = schedule.getIcsCalendarContent();

        byte[] bytes = icsContent.getBytes(StandardCharsets.UTF_8);

        String filename = "ms-admissions-timeline-"
                + (schedule.getCountry() != null ? schedule.getCountry().toLowerCase() : "global")
                + ".ics";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=utf-8")
                .body(bytes);
    }
}
