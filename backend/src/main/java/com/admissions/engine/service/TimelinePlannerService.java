package com.admissions.engine.service;

import com.admissions.engine.model.timeline.*;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service orchestrating backwards-scheduled admissions and visa timelines for Indian MS candidates.
 *
 * <p>Calculates strict deadlines backwards from a program start date for:
 * <ul>
 *   <li>Standardized exams (GRE, IELTS, TOEFL, JLPT with biannual July/Dec constraint)</li>
 *   <li>Credential verifications (German APS certification, US WES evaluation)</li>
 *   <li>University portal deadlines and scholarship submissions (DAAD, MEXT)</li>
 *   <li>Financial proof, Sperrkonto blocked account, and Form I-20/COE issuance</li>
 *   <li>VFS waitlists, biometrics, consular visa appointments, and pre-departure buffer</li>
 * </ul>
 * </p>
 */
@Service
public class TimelinePlannerService {

    private static final DateTimeFormatter ICS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Returns the list of standard supported intake cycles.
     */
    public List<IntakeOption> getIntakeOptions() {
        return List.of(
            new IntakeOption(
                "FALL_2027",
                "Fall 2027 (US / Canada / Europe)",
                "US_CANADA / EUROPE",
                "Germany / USA / Canada",
                "FALL",
                2027,
                LocalDate.of(2027, 9, 1),
                "Primary intake for US, Canada, and European universities. Highest availability of funding, TA/RA assistantships, and course selections."
            ),
            new IntakeOption(
                "SPRING_2027",
                "Spring 2027 (US / Europe)",
                "US_CANADA / EUROPE",
                "USA / Germany",
                "SPRING",
                2027,
                LocalDate.of(2027, 1, 15),
                "Accelerated intake. Ideal for applicants who have already finalized standardized tests and credential verifications."
            ),
            new IntakeOption(
                "JAPAN_APRIL_2027",
                "Japan Spring (April 2027)",
                "JAPAN",
                "Japan",
                "SPRING",
                2027,
                LocalDate.of(2027, 4, 1),
                "Main academic intake in Japan. Crucial for Japanese-taught tracks and MEXT University Recommendation applicants."
            ),
            new IntakeOption(
                "JAPAN_OCTOBER_2027",
                "Japan Autumn (October 2027)",
                "JAPAN",
                "Japan",
                "AUTUMN",
                2027,
                LocalDate.of(2027, 10, 1),
                "Premier intake for international English-taught graduate programs in CS and AI (e.g. Todai GSII, Kyoto Informatics)."
            ),
            new IntakeOption(
                "FALL_2028",
                "Fall 2028 (Long-Range Planning)",
                "US_CANADA / EUROPE",
                "Global",
                "FALL",
                2028,
                LocalDate.of(2028, 9, 1),
                "Extended runway for students currently in 3rd year B.Tech to prepare publications, research proposals, and test scores."
            )
        );
    }

    /**
     * Generates a backwards-scheduled timeline based on the request.
     */
    public TimelineSchedule generateTimeline(TimelineRequest request) {
        String country = resolveCountry(request);
        LocalDate targetStartDate = resolveTargetStartDate(request);
        LocalDate today = LocalDate.now();

        List<MilestoneTemplate> templates = getTemplatesForCountry(country, targetStartDate);
        List<TimelineMilestone> milestones = new ArrayList<>();
        List<String> criticalAlerts = new ArrayList<>();

        int criticalPathCount = 0;
        int overdueCount = 0;
        int urgentCount = 0;

        for (int i = 0; i < templates.size(); i++) {
            MilestoneTemplate t = templates.get(i);
            LocalDate milestoneDate = targetStartDate.plusDays(t.daysOffset);

            // Special handling for JLPT biannual constraint
            if (t.isJlpt) {
                milestoneDate = adjustForJlptSchedule(targetStartDate, t.daysOffset);
            }

            MilestoneStatus status;
            if (milestoneDate.isBefore(today)) {
                status = MilestoneStatus.OVERDUE;
                overdueCount++;
                if (t.criticality == MilestoneCriticality.CRITICAL_PATH) {
                    criticalAlerts.add("CRITICAL DELAY: \"" + t.title + "\" was due on " + milestoneDate
                            + " and is now OVERDUE. Immediate action required to prevent cycle deferral.");
                }
            } else if (milestoneDate.isBefore(today.plusDays(30))) {
                status = MilestoneStatus.URGENT;
                urgentCount++;
            } else {
                status = MilestoneStatus.UPCOMING;
            }

            if (t.criticality == MilestoneCriticality.CRITICAL_PATH) {
                criticalPathCount++;
            }

            String gCalUrl = generateGoogleCalendarUrl(t.title, milestoneDate, t.description, country);

            TimelineMilestone milestone = new TimelineMilestone(
                "MS-" + String.format("%02d", i + 1),
                t.title,
                t.description,
                t.phase,
                t.criticality,
                status,
                milestoneDate,
                t.daysOffset,
                formatLeadTime(t.daysOffset),
                t.indianStudentAdvisory,
                t.actionChecklist,
                t.externalResourceUrl,
                gCalUrl
            );

            milestones.add(milestone);
        }

        // Add country-specific critical warning alerts for Indian candidates
        addCountryAdvisories(country, targetStartDate, today, criticalAlerts);

        String intakeName = resolveIntakeName(request.getIntakeId());
        String summary = generateExecutiveSummary(country, targetStartDate, overdueCount, urgentCount, criticalPathCount);

        TimelineSchedule schedule = new TimelineSchedule(
            request.getIntakeId(),
            intakeName,
            country,
            targetStartDate,
            milestones.size(),
            criticalPathCount,
            overdueCount,
            urgentCount,
            summary,
            criticalAlerts,
            milestones,
            null
        );

        schedule.setIcsCalendarContent(generateIcsCalendar(schedule));
        return schedule;
    }

    /**
     * Builds RFC 5545 iCalendar content.
     */
    public String generateIcsCalendar(TimelineSchedule schedule) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//Antigravity//MS Admissions Matrix Engine//EN\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");
        sb.append("X-WR-CALNAME:MS Admissions & Visa Timeline (").append(escapeIcs(schedule.getCountry())).append(")\r\n");
        sb.append("X-WR-CALDESC:Backwards-scheduled admissions deadlines and visa milestones.\r\n");
        sb.append("X-WR-TIMEZONE:Asia/Kolkata\r\n");

        for (TimelineMilestone m : schedule.getMilestones()) {
            LocalDate d = m.getTargetDate();
            String dateStr = d.format(ICS_DATE_FORMAT);
            String nextDayStr = d.plusDays(1).format(ICS_DATE_FORMAT);

            sb.append("BEGIN:VEVENT\r\n");
            sb.append("UID:").append(m.getId()).append("-").append(dateStr).append("@admissions-engine.local\r\n");
            sb.append("DTSTAMP:").append(LocalDate.now().format(ICS_DATE_FORMAT)).append("T000000Z\r\n");
            sb.append("DTSTART;VALUE=DATE:").append(dateStr).append("\r\n");
            sb.append("DTEND;VALUE=DATE:").append(nextDayStr).append("\r\n");
            sb.append("SUMMARY:").append(m.getPhase().getEmoji()).append(" ").append(escapeIcs(m.getTitle())).append("\r\n");
            
            StringBuilder desc = new StringBuilder();
            desc.append(m.getDescription()).append("\n\n");
            desc.append("Phase: ").append(m.getPhase().getDisplayName()).append("\n");
            desc.append("Priority: ").append(m.getCriticality().getLabel()).append("\n");
            if (m.getIndianStudentAdvisory() != null) {
                desc.append("Advisory: ").append(m.getIndianStudentAdvisory()).append("\n");
            }
            if (m.getExternalResourceUrl() != null) {
                desc.append("Portal Link: ").append(m.getExternalResourceUrl()).append("\n");
            }

            sb.append("DESCRIPTION:").append(escapeIcs(desc.toString())).append("\r\n");
            sb.append("CATEGORIES:Admissions,Visa,StudyAbroad\r\n");
            sb.append("STATUS:CONFIRMED\r\n");
            sb.append("TRANSP:TRANSPARENT\r\n");

            // Alarm 7 days before
            sb.append("BEGIN:VALARM\r\n");
            sb.append("ACTION:DISPLAY\r\n");
            sb.append("DESCRIPTION:Reminder: ").append(escapeIcs(m.getTitle())).append("\r\n");
            sb.append("TRIGGER:-P7D\r\n");
            sb.append("END:VALARM\r\n");

            // Alarm 1 day before
            sb.append("BEGIN:VALARM\r\n");
            sb.append("ACTION:DISPLAY\r\n");
            sb.append("DESCRIPTION:Deadline Tomorrow: ").append(escapeIcs(m.getTitle())).append("\r\n");
            sb.append("TRIGGER:-P1D\r\n");
            sb.append("END:VALARM\r\n");

            sb.append("END:VEVENT\r\n");
        }

        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    // ── Helper methods ────────────────────────────────────────────────────────

    private String resolveCountry(TimelineRequest req) {
        if (req.getTargetCountry() != null && !req.getTargetCountry().isBlank()) {
            return req.getTargetCountry().trim();
        }
        if (req.getIntakeId() != null) {
            if (req.getIntakeId().toUpperCase().contains("JAPAN")) return "Japan";
            if (req.getIntakeId().toUpperCase().contains("EU")) return "Germany";
        }
        return "Germany"; // Default to Germany given high APS bottleneck interest
    }

    private LocalDate resolveTargetStartDate(TimelineRequest req) {
        if (req.getTargetStartDate() != null) {
            return req.getTargetStartDate();
        }
        String id = req.getIntakeId();
        if (id != null) {
            for (IntakeOption opt : getIntakeOptions()) {
                if (opt.getId().equalsIgnoreCase(id.trim())) {
                    return opt.getDefaultStartDate();
                }
            }
        }
        return LocalDate.of(2027, 9, 1); // Default Fall 2027
    }

    private String resolveIntakeName(String intakeId) {
        if (intakeId == null) return "Custom Timeline";
        for (IntakeOption opt : getIntakeOptions()) {
            if (opt.getId().equalsIgnoreCase(intakeId)) {
                return opt.getName();
            }
        }
        return intakeId;
    }

    private String formatLeadTime(int daysOffset) {
        int absDays = Math.abs(daysOffset);
        int months = Math.round(absDays / 30.0f);
        if (months >= 1) {
            return "T - " + months + " Months (" + absDays + " days before start)";
        }
        return "T - " + absDays + " Days before start";
    }

    private String generateGoogleCalendarUrl(String title, LocalDate date, String details, String country) {
        try {
            String dateFormatted = date.format(DateTimeFormatter.BASIC_ISO_DATE);
            String nextDayFormatted = date.plusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE);
            String datesParam = dateFormatted + "/" + nextDayFormatted;
            return "https://calendar.google.com/calendar/render?action=TEMPLATE"
                    + "&text=" + URLEncoder.encode(title, StandardCharsets.UTF_8)
                    + "&dates=" + datesParam
                    + "&details=" + URLEncoder.encode(details, StandardCharsets.UTF_8)
                    + "&location=" + URLEncoder.encode(country, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "https://calendar.google.com/calendar/render";
        }
    }

    private String escapeIcs(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace(";", "\\;")
                   .replace(",", "\\,")
                   .replace("\r\n", "\\n")
                   .replace("\n", "\\n");
    }

    private LocalDate adjustForJlptSchedule(LocalDate targetStartDate, int daysOffset) {
        // JLPT is held on first Sunday of July and first Sunday of December.
        LocalDate desired = targetStartDate.plusDays(daysOffset);
        int year = desired.getYear();

        LocalDate julExam = findFirstSunday(year, 7);
        LocalDate decExam = findFirstSunday(year, 12);

        // If target intake is April, Dec results arrive in Feb (too late for Nov applications)
        // Applicant must take the July exam of the preceding year.
        if (targetStartDate.getMonthValue() == 4) {
            return findFirstSunday(targetStartDate.getYear() - 1, 7);
        }

        // For October intake, December exam of prior year is ideal
        if (targetStartDate.getMonthValue() == 10) {
            return findFirstSunday(targetStartDate.getYear() - 1, 12);
        }

        // Default: pick closest JLPT before desired date
        if (julExam.isBefore(desired)) return julExam;
        return decExam;
    }

    private LocalDate findFirstSunday(int year, int month) {
        LocalDate date = LocalDate.of(year, month, 1);
        while (date.getDayOfWeek() != java.time.DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    private void addCountryAdvisories(String country, LocalDate start, LocalDate today, List<String> alerts) {
        String c = country.toUpperCase();
        if (c.contains("GERMANY") || c.contains("EUROPE")) {
            alerts.add("🇩🇪 APS Certificate Backlog: Indian degree verification at APS New Delhi takes 60–120 days. Initiate application before Uni-Assist deadlines.");
            alerts.add("🛂 German VFS Waitlist: Peak summer (June–August) visa appointment queues require joining the Mission waitlist 12–16 weeks before intended flight date.");
        } else if (c.contains("USA") || c.contains("US") || c.contains("AMERICA")) {
            alerts.add("🇺🇸 F-1 Visa Appointment Rush: US Embassy slots for Fall in India typically open in batches between May and July. Monitor USVisaScheduling.com daily.");
            alerts.add("📜 I-20 & Financial Solvency: Ensure your liquid bank certificate ($60k–$85k) or education loan sanction letter is ready by April to receive your I-20 promptly.");
        } else if (c.contains("JAPAN")) {
            alerts.add("🇯🇵 JLPT Biannual Cycle: The JLPT test happens ONLY in July and December. For April intake, you must take the July test of the prior year.");
            alerts.add("🏛️ Professor Prior Approval (内諾): Japanese graduate admissions require contact with prospective laboratory faculty 8–9 months before entry.");
        }
    }

    private String generateExecutiveSummary(String country, LocalDate start, int overdue, int urgent, int critical) {
        StringBuilder sb = new StringBuilder();
        sb.append("Backwards schedule calculated for ").append(country).append(" target start on ")
          .append(start.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))).append(". ");

        if (overdue > 0) {
            sb.append("⚠️ ").append(overdue).append(" milestone(s) are currently OVERDUE. ");
        }
        if (urgent > 0) {
            sb.append("⏳ ").append(urgent).append(" milestone(s) require action within the next 30 days. ");
        }
        if (overdue == 0 && urgent == 0) {
            sb.append("✅ All milestones are currently on schedule. Maintain steady progress along the critical path. ");
        }
        sb.append("Follow the critical path (").append(critical).append(" non-negotiable steps) to prevent admissions cycle deferral.");
        return sb.toString();
    }

    // ── Milestone Templates Definition ────────────────────────────────────────

    private record MilestoneTemplate(
        String title,
        String description,
        TimelinePhase phase,
        MilestoneCriticality criticality,
        int daysOffset,
        String indianStudentAdvisory,
        List<String> actionChecklist,
        String externalResourceUrl,
        boolean isJlpt
    ) {
        MilestoneTemplate(String title, String description, TimelinePhase phase,
                          MilestoneCriticality criticality, int daysOffset,
                          String indianStudentAdvisory, List<String> actionChecklist,
                          String externalResourceUrl) {
            this(title, description, phase, criticality, daysOffset, indianStudentAdvisory, actionChecklist, externalResourceUrl, false);
        }
    }

    private List<MilestoneTemplate> getTemplatesForCountry(String country, LocalDate start) {
        String c = country.toUpperCase();
        if (c.contains("USA") || c.contains("US") || c.contains("CANADA")) {
            return getUsCanadaTemplates();
        } else if (c.contains("JAPAN")) {
            return getJapanTemplates();
        } else {
            return getGermanyEuropeTemplates();
        }
    }

    private List<MilestoneTemplate> getGermanyEuropeTemplates() {
        return List.of(
            new MilestoneTemplate(
                "Standardized Language Tests (IELTS / TOEFL & German A1)",
                "Appear for IELTS Academic or TOEFL iBT and build German foundational vocabulary.",
                TimelinePhase.TESTS_AND_PREP,
                MilestoneCriticality.HIGH_PRIORITY,
                -340,
                "German public universities require IELTS 6.5–7.0 or TOEFL 90+. Even for English programs, German A1 certificate strengthens visa interview confidence.",
                List.of("Register for IELTS Academic (minimum overall 6.5)", "Collect official electronic TRF score card", "Begin A1 Goethe-Zertifikat preparation"),
                "https://www.ieltsidpindia.com/"
            ),
            new MilestoneTemplate(
                "German APS Certificate Application (Akademische Prüfstelle India)",
                "Submit document verification dossier to APS India in New Delhi. Mandatory requirement for Indian degree holders.",
                TimelinePhase.CREDENTIAL_VERIFICATION,
                MilestoneCriticality.CRITICAL_PATH,
                -300,
                "⚠️ CRITICAL BOTTLENECK: APS India takes 60–120 days. German universities will NOT process applications without APS certificate. Apply at least 10 months ahead.",
                List.of("Register at aps-india.info and pay ₹18,000 fee", "Collect sealed university transcripts & degree certificate", "Provide official professor email for verification", "Courier hard-copy dossier to APS Office New Delhi"),
                "https://www.aps-india.info/"
            ),
            new MilestoneTemplate(
                "DAAD & European Scholarship Submissions",
                "Apply for DAAD Master Studies Scholarships and regional tuition subsidies.",
                TimelinePhase.UNIVERSITY_APPLICATIONS,
                MilestoneCriticality.HIGH_PRIORITY,
                -240,
                "DAAD scholarships (e.g. EPOS, Helmut-Schmidt) provide €934/month stipend and travel allowance, with deadlines up to 10 months prior to study.",
                List.of("Identify DAAD scholarship programs in CS/AI", "Secure 2 academic recommendation letters on university letterhead", "Submit DAAD portal application"),
                "https://www2.daad.de/deutschland/stipendien/datenbank/en/21148-scholarship-database/"
            ),
            new MilestoneTemplate(
                "Uni-Assist VPD & Direct University Portal Submissions",
                "Submit preliminary documentation (VPD) via Uni-Assist and complete university online applications.",
                TimelinePhase.UNIVERSITY_APPLICATIONS,
                MilestoneCriticality.CRITICAL_PATH,
                -210,
                "Uni-Assist VPD evaluation takes 4–6 weeks. Apply at least 1 month before university deadlines (e.g. TUM international cutoff is May 31).",
                List.of("Upload transcripts, APS certificate, and CV to Uni-Assist", "Draft tailored Statement of Motivation for each master's program", "Submit university portal forms"),
                "https://www.uni-assist.de/en/"
            ),
            new MilestoneTemplate(
                "Sperrkonto (Blocked Account €11,904) & Health Insurance Setup",
                "Open an approved German blocked account and secure statutory student health insurance (TK / Barmer).",
                TimelinePhase.FINANCES_AND_ADMISSION,
                MilestoneCriticality.CRITICAL_PATH,
                -130,
                "Deposit €11,904 via outward remittance (A2 form via Indian bank). Sperrbestätigung confirmation document is mandatory for visa appointment.",
                List.of("Open blocked account via Coracle, Expatrio, or Fintiba", "Execute international wire transfer with bank swift advice", "Receive official Sperrbestätigung letter", "Enroll in statutory health insurance"),
                "https://www.auswaertiges-amt.de/en/visa-service/buergerservice/faq/02-sperrkonto"
            ),
            new MilestoneTemplate(
                "German National Student Visa Appointment Booking (VFS Waitlist)",
                "Register on the German Mission India appointment waitlist and prepare VIDEX application dossier.",
                TimelinePhase.VISA_AND_EMBASSY,
                MilestoneCriticality.CRITICAL_PATH,
                -120,
                "⚠️ VFS WAITLIST ALERT: In peak summer, German student visa appointment waitlists in Delhi, Mumbai, Bengaluru, and Chennai reach 8–12 weeks. Join waitlist immediately upon admission.",
                List.of("Register on German Mission waitlist portal", "Fill VIDEX national visa online form (2 copies)", "Prepare biometric passport photos (ICAO standards)", "Assemble dossier in exact embassy order"),
                "https://india.diplo.de/in-en/service/-/1805324"
            ),
            new MilestoneTemplate(
                "Visa Interview & Biometrics at VFS Global",
                "Attend biometric capture and visa interview at VFS Global Visa Application Centre.",
                TimelinePhase.VISA_AND_EMBASSY,
                MilestoneCriticality.HIGH_PRIORITY,
                -75,
                "Processing takes 4–8 weeks. Submit original passport, admission letter, APS certificate, and blocked account confirmation.",
                List.of("Attend biometric enrollment", "Submit original passport and sealed document dossier", "Track passport delivery via blue dart / courier"),
                "https://visa.vfsglobal.com/ind/en/deu"
            ),
            new MilestoneTemplate(
                "Studentenwerk Dormitory Application & Travel Booking",
                "Secure student accommodation (WG / Studentenwerk) and book international student flight.",
                TimelinePhase.PRE_DEPARTURE,
                MilestoneCriticality.STANDARD,
                -25,
                "German student dorms have long waitlists. Apply to local Studentenwerk months early. Book student fare flights with 40kg+ baggage.",
                List.of("Apply for Studentenwerk student housing", "Book student flight to Frankfurt / Munich / Berlin", "Carry €1,000 cash for initial expenses", "Book appointment for City Address Registration (Anmeldung)"),
                "https://www.studentenwerke.de/en"
            )
        );
    }

    private List<MilestoneTemplate> getUsCanadaTemplates() {
        return List.of(
            new MilestoneTemplate(
                "GRE & TOEFL / IELTS Standardized Tests",
                "Complete GRE General exam and English proficiency tests with official score reporting to universities.",
                TimelinePhase.TESTS_AND_PREP,
                MilestoneCriticality.HIGH_PRIORITY,
                -330,
                "Aim for GRE Quant 165+ for CS/AI. Allow 3–4 weeks for ETS official electronic score delivery to university institution codes.",
                List.of("Take GRE exam and send 4 free score reports", "Complete TOEFL iBT (100+) or IELTS Academic (7.5+)", "Record ETS registration numbers"),
                "https://www.ets.org/gre"
            ),
            new MilestoneTemplate(
                "Transcript Attestation & WES / ECE Credential Evaluation",
                "Order university sealed transcripts and initiate WES course-by-course evaluation.",
                TimelinePhase.CREDENTIAL_VERIFICATION,
                MilestoneCriticality.HIGH_PRIORITY,
                -290,
                "Many US universities require WES/ECE evaluation for 3-year or 4-year Indian bachelor degrees. Takes 4–8 weeks total.",
                List.of("Create WES account and obtain reference number", "Request home university to send sealed physical or electronic transcripts", "Verify course-by-course GPA conversion"),
                "https://www.wes.org/"
            ),
            new MilestoneTemplate(
                "University Portal Applications (Early & Priority Deadlines)",
                "Submit online applications, Statements of Purpose, and upload Letters of Recommendation.",
                TimelinePhase.UNIVERSITY_APPLICATIONS,
                MilestoneCriticality.CRITICAL_PATH,
                -250,
                "Top US CS programs have strict hard deadlines between Dec 1 and Jan 15. Priority deadlines maximize scholarship and TA/RA consideration.",
                List.of("Finalize Statement of Purpose (SOP)", "Ensure 3 professors submit Letters of Recommendation (LOR)", "Submit university online forms and pay application fees"),
                "https://www.commonapp.org/"
            ),
            new MilestoneTemplate(
                "Admissions Acceptance & Enrollment Seat Deposit",
                "Accept university admission offer and pay non-refundable enrollment deposit by April 15.",
                TimelinePhase.FINANCES_AND_ADMISSION,
                MilestoneCriticality.HIGH_PRIORITY,
                -135,
                "April 15 is the National Decision Day (CGS Resolution) across US universities.",
                List.of("Compare admits and departmental funding packages", "Formally accept admission offer", "Pay enrollment seat deposit ($500–$2,000)"),
                "https://cgsnet.org/"
            ),
            new MilestoneTemplate(
                "Financial Affidavit & Form I-20 Issuance by University DSO",
                "Submit liquid financial solvency documents ($60k–$85k) to obtain Form I-20.",
                TimelinePhase.FINANCES_AND_ADMISSION,
                MilestoneCriticality.CRITICAL_PATH,
                -120,
                "University DSO issues Certificate of Eligibility (Form I-20) needed for F-1 visa. Requires bank certificate or loan sanction letter.",
                List.of("Obtain education loan sanction letter / bank balance certificate", "Complete university Affidavit of Financial Support", "Receive official signed Form I-20 with SEVIS ID N00..."),
                "https://studyinthestates.dhs.gov/students"
            ),
            new MilestoneTemplate(
                "Pay SEVIS I-901 Fee & Submit Form DS-160",
                "Pay $350 SEVIS fee and complete online nonimmigrant visa application (DS-160).",
                TimelinePhase.VISA_AND_EMBASSY,
                MilestoneCriticality.HIGH_PRIORITY,
                -100,
                "Pay SEVIS fee on FMJfee.com. Submit DS-160 for your consular jurisdiction in India (Delhi, Mumbai, Chennai, Hyderabad, Kolkata).",
                List.of("Pay $350 SEVIS I-901 fee and print receipt", "Complete DS-160 form accurately", "Save DS-160 confirmation barcode page"),
                "https://www.fmjfee.com/"
            ),
            new MilestoneTemplate(
                "F-1 Visa Slot Booking & Consular Interview (VAC + Embassy)",
                "Book biometrics (VAC) and Consular visa interview appointments on USVisaScheduling.com.",
                TimelinePhase.VISA_AND_EMBASSY,
                MilestoneCriticality.CRITICAL_PATH,
                -75,
                "⚠️ US VISA APPOINTMENT CRUSH: Student visa slots open in batches May–July. Attend biometric and in-person consular interview.",
                List.of("Book VAC and Consular interview dates", "Assemble visa folder: I-20, SEVIS receipt, DS-160, financial proofs, transcripts", "Practice F-1 interview questions explaining non-immigrant intent"),
                "https://www.usvisascheduling.com/"
            ),
            new MilestoneTemplate(
                "Port of Entry Arrival (Within Legal 30-Day SEVP Window)",
                "Travel to the United States up to 30 days prior to program start date on Form I-20.",
                TimelinePhase.PRE_DEPARTURE,
                MilestoneCriticality.STANDARD,
                -20,
                "F-1 regulations prohibit entering the US earlier than 30 days before program start. Carry original I-20 in cabin baggage.",
                List.of("Book flight for arrival 2–3 weeks before orientation", "Carry physical I-20, passport with F-1 visa stamp, financial proofs", "Complete university immunization and health insurance enrollment"),
                "https://www.ice.gov/sevis"
            )
        );
    }

    private List<MilestoneTemplate> getJapanTemplates() {
        return List.of(
            new MilestoneTemplate(
                "JLPT (Japanese Language Proficiency Test) Registration & Exam",
                "Appear for the JLPT exam (N3 or N2 level). Essential for laboratory integration and MEXT scholarship points.",
                TimelinePhase.TESTS_AND_PREP,
                MilestoneCriticality.CRITICAL_PATH,
                -300,
                "⚠️ CRITICAL BIANNUAL CYCLE: JLPT is ONLY held in July and December. For April intake, you must take the July exam of the previous year. Results take 7 weeks.",
                List.of("Register for JLPT exam (March for July test, August for Dec test)", "Target N3 minimum for bilingual labs or N2 for Japanese-track programs", "Obtain official score certificate from Japan Foundation"),
                "https://www.jlpt.jp/e/",
                true
            ),
            new MilestoneTemplate(
                "Research Proposal (Kenkyu-keikakusho) & Lab Supervisor Approval (内諾 Naidaku)",
                "Draft comprehensive research proposal and contact prospective laboratory professors for informal admission consent.",
                TimelinePhase.UNIVERSITY_APPLICATIONS,
                MilestoneCriticality.CRITICAL_PATH,
                -260,
                "In Japan, graduate STEM programs require informal supervisor acceptance (内諾, Naidaku) before submitting official applications.",
                List.of("Draft 3-page research proposal aligned with target laboratory papers", "Email prospective professors with CV, transcripts, and proposal", "Conduct Zoom interview with professor to secure sponsorship"),
                "https://www.studyinjapan.go.jp/en/"
            ),
            new MilestoneTemplate(
                "MEXT University Recommendation / JASSO Scholarship Submission",
                "Apply for MEXT Japanese Government Scholarship through university nomination.",
                TimelinePhase.FINANCES_AND_ADMISSION,
                MilestoneCriticality.HIGH_PRIORITY,
                -200,
                "MEXT University Recommendation provides full tuition waiver and ¥144,000/month living stipend.",
                List.of("Submit MEXT scholarship application forms via host university", "Provide official medical certificate (Certificate of Health)", "Submit certified graduation certificate"),
                "https://www.mext.go.jp/en/"
            ),
            new MilestoneTemplate(
                "Formal Graduate School Application & Entrance Exam / Interview",
                "Submit official university admissions forms and defend research proposal in oral exam.",
                TimelinePhase.UNIVERSITY_APPLICATIONS,
                MilestoneCriticality.HIGH_PRIORITY,
                -180,
                "Submit verified documents, English test scores (TOEFL/IELTS), and attend oral defense examination.",
                List.of("Submit application dossier to university Graduate School office", "Pay examination fee (¥30,000)", "Attend online oral defense of research proposal"),
                "https://www.jasso.go.jp/en/"
            ),
            new MilestoneTemplate(
                "Certificate of Eligibility (COE) Filing by Host University",
                "University applies for COE with Immigration Services Agency of Japan on your behalf.",
                TimelinePhase.FINANCES_AND_ADMISSION,
                MilestoneCriticality.CRITICAL_PATH,
                -120,
                "⚠️ JAPAN IMMIGRATION BUFFER: Japanese Immigration takes 60–90 days to issue the Certificate of Eligibility (在留資格認定証明書). Financial proof of ¥2M+ required.",
                List.of("Submit bank balance certificate (¥2,000,000+) to university", "Submit sponsor income tax certificates", "University files COE with regional immigration bureau"),
                "https://www.isa.go.jp/en/"
            ),
            new MilestoneTemplate(
                "Student Visa Stamping at VFS Japan in India",
                "Submit passport and original/electronic COE at VFS Japan for visa issuance.",
                TimelinePhase.VISA_AND_EMBASSY,
                MilestoneCriticality.HIGH_PRIORITY,
                -35,
                "Once the COE arrives from Japan, visa issuance at VFS Japan takes only 5–7 working days.",
                List.of("Submit passport, original/electronic COE, and application form", "Provide 45mm x 35mm photos", "Collect stamped passport with College Student (Ryugaku) visa"),
                "https://visa.vfsglobal.com/ind/en/jpn"
            ),
            new MilestoneTemplate(
                "Arrival in Japan & Residence Card (Zairyu Card) + Work Permit Setup",
                "Arrive in Japan, receive Residence Card at airport, and apply for part-time work permit.",
                TimelinePhase.PRE_DEPARTURE,
                MilestoneCriticality.STANDARD,
                -14,
                "Apply for Permission to Engage in Other Activity (Shikakugai Katsudo Kyoka) at airport immigration to legally work up to 28 hrs/week.",
                List.of("Submit work permit form at airport immigration upon landing", "Receive physical Residence Card (在留カード)", "Register address at local Ward Office (Kuyakusho) within 14 days", "Enroll in National Health Insurance (Kokumin Kenko Hoken)"),
                "https://www.isa.go.jp/en/applications/procedures/index.html"
            )
        );
    }
}
