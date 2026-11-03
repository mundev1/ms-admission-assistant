package com.admissions.engine.service;

import com.admissions.engine.model.ApplicantProfile;
import com.admissions.engine.model.scholarship.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service orchestrating the Global Scholarship & Assistantship Radar.
 * Auto-matches student academic profile, GPA, and regional preferences with
 * major government, foundation, and institutional funding opportunities.
 */
@Service
public class ScholarshipRadarService {

    private final List<ScholarshipOpportunity> curatedCatalog = new ArrayList<>();

    public ScholarshipRadarService() {
        initializeCuratedScholarships();
    }

    /**
     * Retrieves the entire curated catalogue of international scholarships.
     */
    public List<ScholarshipOpportunity> getAllOpportunities() {
        return Collections.unmodifiableList(curatedCatalog);
    }

    /**
     * Retrieves a single scholarship opportunity by ID.
     */
    public Optional<ScholarshipOpportunity> getOpportunityById(String id) {
        return curatedCatalog.stream()
                .filter(s -> s.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    /**
     * Executes the comprehensive radar scan against an applicant's profile.
     */
    public ScholarshipRadarResult scanRadar(ScholarshipRadarRequest request) {
        ApplicantProfile profile = request.getProfile();
        if (profile == null) {
            profile = new ApplicantProfile(); // Default empty profile
        }

        String targetRegion = (request.getTargetRegion() != null)
                ? request.getTargetRegion().toUpperCase().trim() : "ALL";
        String minCoverage = (request.getMinCoverageType() != null)
                ? request.getMinCoverageType().toUpperCase().trim() : "ALL";

        List<ScholarshipMatchResult> matches = new ArrayList<>();

        for (ScholarshipOpportunity opp : curatedCatalog) {
            // Apply region filter if specified
            if (!"ALL".equals(targetRegion)) {
                if (!matchesRegionFilter(opp.getRegion(), targetRegion)) {
                    continue;
                }
            }

            // Apply coverage filter if specified
            if (!"ALL".equals(minCoverage)) {
                if (!opp.getCoverageType().equalsIgnoreCase(minCoverage)) {
                    continue;
                }
            }

            ScholarshipMatchResult match = evaluateApplicantForScholarship(profile, opp);
            matches.add(match);
        }

        // Sort descending by match score, then by financial value
        matches.sort((a, b) -> {
            int scoreCmp = Double.compare(b.getMatchScore(), a.getMatchScore());
            if (scoreCmp != 0) return scoreCmp;
            return Double.compare(b.getOpportunity().getFinancialValueUsd(), a.getOpportunity().getFinancialValueUsd());
        });

        // Compute aggregate metrics
        double totalQualifiedValue = 0.0;
        int qualifiedCount = 0;
        int strongCandidateCount = 0;
        int fullRideCount = 0;

        Map<String, List<ScholarshipMatchResult>> categorized = new LinkedHashMap<>();
        categorized.put("GERMANY", new ArrayList<>());
        categorized.put("JAPAN", new ArrayList<>());
        categorized.put("US_UK", new ArrayList<>());
        categorized.put("EUROPE", new ArrayList<>());

        for (ScholarshipMatchResult m : matches) {
            String region = m.getOpportunity().getRegion();
            if (categorized.containsKey(region)) {
                categorized.get(region).add(m);
            } else {
                categorized.computeIfAbsent("EUROPE", k -> new ArrayList<>()).add(m);
            }

            if ("DIRECT_MATCH".equals(m.getEligibilityStatus())) {
                qualifiedCount++;
                totalQualifiedValue += m.getOpportunity().getFinancialValueUsd();
            } else if ("STRONG_CANDIDATE".equals(m.getEligibilityStatus())) {
                strongCandidateCount++;
                totalQualifiedValue += (m.getOpportunity().getFinancialValueUsd() * 0.75);
            }

            if ("FULL_RIDE".equals(m.getOpportunity().getCoverageType()) && m.getMatchScore() >= 60.0) {
                fullRideCount++;
            }
        }

        // Evaluate US/UK Assistantship (TA/RA) Fee Remission
        AssistantshipEvaluation assistantship = evaluateAssistantship(profile);

        // Build strategic recommendations
        List<String> recommendations = generateStrategicRecommendations(profile, matches, assistantship);

        ScholarshipRadarResult result = new ScholarshipRadarResult();
        result.setApplicantName(profile.getFullName() != null ? profile.getFullName() : "Prospective Graduate Scholar");
        result.setTotalEligibleScholarshipValueUsd(Math.round(totalQualifiedValue));
        result.setTotalQualifiedAwards(qualifiedCount);
        result.setTotalStrongCandidateAwards(strongCandidateCount);
        result.setTotalAwardsEvaluated(matches.size());
        result.setFullRideCount(fullRideCount);
        result.setCategorizedMatches(categorized);
        result.setAllMatches(matches);
        result.setAssistantshipEvaluation(assistantship);
        result.setStrategicRecommendations(recommendations);

        return result;
    }

    /**
     * Evaluates a single applicant profile against a scholarship opportunity.
     */
    public ScholarshipMatchResult evaluateApplicantForScholarship(ApplicantProfile p, ScholarshipOpportunity opp) {
        double cgpa = p.getCgpa10pt() > 0 ? p.getCgpa10pt() : 7.5;
        int workMonths = (p.getInternshipMonths() != null ? p.getInternshipMonths() : 0);
        boolean thesis = Boolean.TRUE.equals(p.getThesisCompleted());
        int publications = p.getPublicationsCount() != null ? p.getPublicationsCount() : 0;
        int greQ = p.getGreQuant() != null ? p.getGreQuant() : 0;
        int toefl = p.getToeflScore() != null ? p.getToeflScore() : 0;
        double ielts = p.getIeltsScore() != null ? p.getIeltsScore() : 0.0;
        String jlpt = p.getJlptLevel() != null ? p.getJlptLevel().toUpperCase().trim() : "NONE";

        List<String> satisfied = new ArrayList<>();
        List<String> unmet = new ArrayList<>();
        List<String> advice = new ArrayList<>();

        double criteriaWeightTotal = 0.0;
        double criteriaScoreEarned = 0.0;

        // 1. CGPA Metric (Weight: 40)
        criteriaWeightTotal += 40.0;
        if (cgpa >= opp.getMinCgpa()) {
            satisfied.add(String.format("Academic Merit: CGPA %.2f meets or exceeds requirement (>= %.2f)", cgpa, opp.getMinCgpa()));
            criteriaScoreEarned += 40.0;
            if (cgpa >= opp.getMinCgpa() + 0.6) {
                satisfied.add("Exceptional GPA Bonus: Top tier distinction gives competitive scoring edge");
            }
        } else {
            double gap = opp.getMinCgpa() - cgpa;
            if (gap <= 0.4) {
                unmet.add(String.format("Borderline CGPA: Current %.2f vs required %.2f (offset with GRE or publications)", cgpa, opp.getMinCgpa()));
                criteriaScoreEarned += 25.0;
            } else {
                unmet.add(String.format("CGPA Deficit: Current %.2f falls below minimum required threshold of %.2f", cgpa, opp.getMinCgpa()));
                criteriaScoreEarned += Math.max(5.0, 40.0 - (gap * 25.0));
            }
        }

        // 2. Work / Internship Experience (Weight: 25)
        if (opp.getMinWorkExperienceMonths() > 0) {
            criteriaWeightTotal += 25.0;
            if (workMonths >= opp.getMinWorkExperienceMonths()) {
                satisfied.add(String.format("Experience Requirement: %d months verified (exceeds %d months minimum)",
                        workMonths, opp.getMinWorkExperienceMonths()));
                criteriaScoreEarned += 25.0;
            } else {
                int gapMonths = opp.getMinWorkExperienceMonths() - workMonths;
                unmet.add(String.format("Professional Experience Gap: %d months completed vs %d months mandatory (shortfall: %d months)",
                        workMonths, opp.getMinWorkExperienceMonths(), gapMonths));
                double expRatio = (double) workMonths / opp.getMinWorkExperienceMonths();
                criteriaScoreEarned += Math.round(25.0 * expRatio);
                advice.add(String.format("Gain %d additional months of relevant industry/research experience before the intake deadline", gapMonths));
            }
        }

        // 3. Research & Bachelor Thesis (Weight: 15)
        if (opp.isRequiresThesis()) {
            criteriaWeightTotal += 15.0;
            if (thesis || publications > 0) {
                satisfied.add(String.format("Research Capability: %s verified (Publications: %d)",
                        thesis ? "Bachelor Thesis completed" : "Peer-reviewed research", publications));
                criteriaScoreEarned += 15.0;
            } else {
                unmet.add("Research Track: Requires documented undergraduate thesis or published conference paper");
                criteriaScoreEarned += 0.0;
                advice.add("Submit preprint or convert capstone engineering project into an arXiv paper or technical whitepaper");
            }
        }

        // 4. Standardized Test Requirements (Weight: 20)
        criteriaWeightTotal += 20.0;
        double testScore = 20.0;

        if (opp.getMinGreQuant() != null && opp.getMinGreQuant() > 0) {
            if (greQ >= opp.getMinGreQuant()) {
                satisfied.add(String.format("GRE Quantitative: %d meets target (>= %d)", greQ, opp.getMinGreQuant()));
            } else if (greQ > 0) {
                unmet.add(String.format("GRE Quant: %d is below competitive benchmark of %d", greQ, opp.getMinGreQuant()));
                testScore -= 8.0;
                advice.add(String.format("Retake GRE to raise Quantitative section from %d to %d+", greQ, opp.getMinGreQuant()));
            } else {
                unmet.add(String.format("GRE Quant: Score required (benchmark >= %d)", opp.getMinGreQuant()));
                testScore -= 10.0;
            }
        }

        if (opp.getMinToefl() != null && opp.getMinToefl() > 0) {
            boolean englishSatisfied = (toefl >= opp.getMinToefl()) || (ielts >= (opp.getMinIelts() != null ? opp.getMinIelts() : 7.0));
            if (englishSatisfied) {
                satisfied.add(String.format("English Proficiency: TOEFL %d / IELTS %.1f meets requirements",
                        Math.max(toefl, 0), Math.max(ielts, 0.0)));
            } else {
                unmet.add(String.format("English Test: Requires TOEFL >= %d or IELTS >= %.1f",
                        opp.getMinToefl(), opp.getMinIelts() != null ? opp.getMinIelts() : 7.0));
                testScore -= 6.0;
            }
        }

        if (opp.getRequiresJlpt() != null && !opp.getRequiresJlpt().isEmpty()) {
            if ("N1".equals(jlpt) || "N2".equals(jlpt)) {
                satisfied.add(String.format("Japanese Language: JLPT %s provides strong competitive advantage", jlpt));
            } else if ("N3".equals(jlpt) || "N4".equals(jlpt) || "N5".equals(jlpt)) {
                satisfied.add(String.format("Japanese Language: JLPT %s satisfies foundational criteria for English-track programs", jlpt));
            } else {
                advice.add("For Japanese Government MEXT, acquiring JLPT N5/N4 before arrival boosts embassy interview ranking");
            }
        }

        criteriaScoreEarned += Math.max(0.0, testScore);

        // Normalize total score to 0 - 100%
        double rawMatchScore = (criteriaWeightTotal > 0)
                ? (criteriaScoreEarned / criteriaWeightTotal) * 100.0 : 75.0;
        double finalScore = Math.round(Math.min(100.0, Math.max(10.0, rawMatchScore)) * 10.0) / 10.0;

        // Classify Eligibility Status
        String status;
        String label;
        if (finalScore >= 85.0 && unmet.isEmpty()) {
            status = "DIRECT_MATCH";
            label = "Direct Match & High Eligibility";
        } else if (finalScore >= 70.0) {
            status = "STRONG_CANDIDATE";
            label = "Strong Candidate — High Odds";
        } else if (finalScore >= 50.0) {
            status = "COMPETITIVE_WITH_GAP";
            label = "Competitive (Remedial Gaps)";
        } else {
            status = "NOT_ELIGIBLE";
            label = "Prerequisites Currently Not Met";
        }

        // Add top tips from opportunity
        advice.addAll(opp.getKeyTips());

        ScholarshipMatchResult result = new ScholarshipMatchResult();
        result.setOpportunity(opp);
        result.setMatchScore(finalScore);
        result.setEligibilityStatus(status);
        result.setStatusLabel(label);
        result.setEstimatedAwardValueUsd(opp.getFinancialValueUsd());
        result.setSatisfiedRequirements(satisfied);
        result.setUnmetRequirements(unmet);
        result.setActionableAdvice(advice);

        return result;
    }

    /**
     * Evaluates US/UK Graduate Teaching Assistantship (TA) and Research Assistantship (RA)
     * fee remission rules and calculates realistic financial value.
     */
    public AssistantshipEvaluation evaluateAssistantship(ApplicantProfile p) {
        double cgpa = p.getCgpa10pt() > 0 ? p.getCgpa10pt() : 7.5;
        int greQ = p.getGreQuant() != null ? p.getGreQuant() : 0;
        int toefl = p.getToeflScore() != null ? p.getToeflScore() : 0;
        double ielts = p.getIeltsScore() != null ? p.getIeltsScore() : 0.0;
        boolean thesis = Boolean.TRUE.equals(p.getThesisCompleted());
        int publications = p.getPublicationsCount() != null ? p.getPublicationsCount() : 0;

        boolean taEligible = (cgpa >= 8.0) && (greQ >= 162 || greQ == 0) && (toefl >= 100 || ielts >= 7.5);
        boolean raEligible = (cgpa >= 8.0) && (thesis || publications >= 1 || (p.getResearchScore() != null && p.getResearchScore() >= 70.0));

        // Average US/UK Master's tuition remission + 20hr/week departmental stipend
        double tuitionRemissionUsd = (taEligible || raEligible) ? 38500.0 : 0.0;
        double monthlyStipendUsd = (taEligible || raEligible) ? 2450.0 : 0.0;
        double annualStipendUsd = monthlyStipendUsd * 9.0; // 9-month standard academic year
        double totalPackage = tuitionRemissionUsd + annualStipendUsd;

        List<String> met = new ArrayList<>();
        List<String> pending = new ArrayList<>();
        List<String> steps = new ArrayList<>();

        if (cgpa >= 8.0) {
            met.add(String.format("GPA Benchmark: CGPA %.2f qualifies for departmental assistantship appointment pool", cgpa));
        } else {
            pending.add(String.format("GPA: Current %.2f is under the 8.0 target typical for guaranteed first-semester TA/RA offers", cgpa));
        }

        if (toefl >= 100 || ielts >= 7.5) {
            met.add("Spoken English Certification: TOEFL/IELTS satisfies university GTA spoken proficiency criteria");
        } else {
            pending.add("TA English Requirement: Most US universities require TOEFL Speaking >= 26 or IELTS Speaking >= 8.0 to teach undergraduate labs");
        }

        if (thesis || publications >= 1) {
            met.add(String.format("Research Track: Proven output (%s) makes profile prime for Graduate Research Assistantships (GRA)",
                    thesis ? "Bachelor Thesis" : "Publications"));
        } else {
            pending.add("RA Experience: No prior thesis or published paper on record. Target faculty alignment early");
        }

        if (taEligible || raEligible) {
            steps.add("Reach out directly to university Graduate Program Directors (GPD) 3 months prior to intake");
            steps.add("Highlight teaching proficiency in core CS courses (Data Structures, Algorithms, Discrete Math)");
            steps.add("Secure 100% Tuition Remission waiver + $2,450/month departmental stipend");
        } else {
            steps.add("Aim for a second-semester TA/RA by securing a 3.8+ graduate GPA in your first semester on campus");
            steps.add("Network in person with professors whose lab seminars you attend in your first term");
        }

        AssistantshipEvaluation eval = new AssistantshipEvaluation();
        eval.setTaEligible(taEligible);
        eval.setRaEligible(raEligible);
        eval.setEstimatedTuitionRemissionUsd(tuitionRemissionUsd);
        eval.setEstimatedMonthlyStipendUsd(monthlyStipendUsd);
        eval.setEstimatedAnnualStipendUsd(annualStipendUsd);
        eval.setEstimatedTotalPackageUsd(totalPackage);
        eval.setWeeklyWorkHours(20);
        eval.setSpeakingExamStatus((toefl >= 100 || ielts >= 7.5) ? "QUALIFIED" : "SPEAKING_TEST_RECOMMENDED");
        eval.setResearchReadinessStatus((thesis || publications >= 1) ? "RESEARCH_READY" : "CAPSTONE_PROJECT_REQUIRED");
        eval.setPrerequisitesMet(met);
        eval.setPrerequisitesPending(pending);
        eval.setActionSteps(steps);

        return eval;
    }

    /**
     * Generates tailored strategic recommendations for Indian graduate candidates.
     */
    private List<String> generateStrategicRecommendations(ApplicantProfile profile,
                                                          List<ScholarshipMatchResult> matches,
                                                          AssistantshipEvaluation assistantship) {
        List<String> recs = new ArrayList<>();

        long directMatches = matches.stream().filter(m -> "DIRECT_MATCH".equals(m.getEligibilityStatus())).count();
        if (directMatches > 0) {
            recs.add(String.format("Direct Qualification: You are competitive for %d premier international awards. Prioritize their independent portal deadlines immediately.", directMatches));
        }

        // Germany DAAD / Deutschlandstipendium recommendation
        boolean germanyInterested = matches.stream().anyMatch(m -> "GERMANY".equals(m.getOpportunity().getRegion()) && m.getMatchScore() >= 70.0);
        if (germanyInterested) {
            recs.add("German Funding Strategy: Apply for the DAAD scholarship before German university portal deadlines (DAAD dead-lines close in October/November). Also apply for Deutschlandstipendium (€300/mo) directly at your matriculating university.");
        }

        // Japan MEXT Recommendation
        boolean japanMatch = matches.stream().anyMatch(m -> "JAPAN".equals(m.getOpportunity().getRegion()) && m.getMatchScore() >= 70.0);
        if (japanMatch) {
            recs.add("Japan MEXT Strategy: Target the Embassy Recommendation Track via the Embassy of Japan in New Delhi (applications open in April), or reach out to target professors for University Recommendation (Naidaku) before December.");
        }

        // US & UK Assistantship & Fulbright
        if (assistantship.isTaEligible() || assistantship.isRaEligible()) {
            recs.add(String.format("US/UK Assistantship Leverage: Your academic metrics qualify for Departmental TA/RA fee remission worth an estimated $%,.0f total funding package.",
                    assistantship.getEstimatedTotalPackageUsd()));
        }

        // Erasmus Mundus Recommendation
        boolean erasmusMatch = matches.stream().anyMatch(m -> "ERASMUS_MUNDUS_EMJM".equals(m.getOpportunity().getId()) && m.getMatchScore() >= 65.0);
        if (erasmusMatch) {
            recs.add("Europe Erasmus Mundus (EMJM): India is among the highest-awarded partner countries. Apply to up to 3 Erasmus Mundus consortia between October and January for full-ride €1,400/month funding across 2+ EU nations.");
        }

        return recs;
    }

    private boolean matchesRegionFilter(String oppRegion, String targetRegion) {
        if ("ALL".equalsIgnoreCase(targetRegion)) return true;
        if (oppRegion == null) return false;
        if (targetRegion.equalsIgnoreCase(oppRegion)) return true;
        if ("US".equalsIgnoreCase(targetRegion) || "USA".equalsIgnoreCase(targetRegion) || "UK".equalsIgnoreCase(targetRegion)) {
            return "US_UK".equalsIgnoreCase(oppRegion);
        }
        if ("EU".equalsIgnoreCase(targetRegion) || "EUROPEAN_UNION".equalsIgnoreCase(targetRegion)) {
            return "EUROPE".equalsIgnoreCase(oppRegion) || "GERMANY".equalsIgnoreCase(oppRegion);
        }
        return false;
    }

    /**
     * Seeds the curated catalog of major global scholarships with verified parameters.
     */
    private void initializeCuratedScholarships() {
        // ─────────────────────────────────────────────────────────────────────
        // 1. GERMANY
        // ─────────────────────────────────────────────────────────────────────

        // DAAD EPOS
        curatedCatalog.add(new ScholarshipOpportunity(
                "DAAD_EPOS",
                "DAAD EPOS (Development-Related Postgraduate Courses)",
                "German Academic Exchange Service (DAAD)",
                "Germany",
                "GERMANY",
                "FULL_RIDE",
                32500.0,
                "€934/month living stipend + return airfare + comprehensive health, accident and liability insurance + €460/yr study allowance",
                7.5,
                24,     // Min 2 years (24 months) professional work experience
                false,
                null,
                90,
                6.5,
                null,
                "August – October (annually, program-specific)",
                "Direct application via the target German university portal with DAAD EPOS application form",
                "https://www.daad.de/en/information-services-for-higher-education-institutions/further-programmes/epos/",
                List.of(
                        "Bachelor's degree completed within the last 6 years with above-average GPA (min 7.5/10)",
                        "At least 2 years (24 months) of relevant professional work experience after bachelor's completion",
                        "Resident/citizen of an ODA/DAC listed developing country (India is fully eligible)",
                        "Clear development-related motivation statement connecting study plan to socio-economic growth"
                ),
                List.of(
                        "Obtain employer recommendation and proof of employment certificates early",
                        "Clearly articulate how your CS/Data/Engineering research solves real developmental challenges in India"
                ),
                List.of("Germany", "DAAD", "Full-Ride", "Work-Experience", "CS-Development")
        ));

        // DAAD Helmut-Schmidt
        curatedCatalog.add(new ScholarshipOpportunity(
                "DAAD_HELMUT_SCHMIDT",
                "DAAD Helmut-Schmidt-Programme (Public Policy & Good Governance)",
                "German Academic Exchange Service (DAAD)",
                "Germany",
                "GERMANY",
                "FULL_RIDE",
                31000.0,
                "Full tuition waiver + €934/month living allowance + health insurance + travel subsidies + 6-month preparatory German language course",
                7.5,
                6,      // Internships / civic engagement
                false,
                null,
                85,
                6.5,
                null,
                "June 1 – July 31 (annually)",
                "Submit DAAD application directly to the participating Master's course of choice",
                "https://www.daad.de/en/information-services-for-higher-education-institutions/further-programmes/helmut-schmidt-programme/",
                List.of(
                        "First university degree with above-average grades in CS, IT, Law, Economics, or Social Sciences",
                        "Demonstrated commitment to tech ethics, digital governance, AI policy, or public administration",
                        "Active social, civic, or student leadership track record"
                ),
                List.of(
                        "Focus SOP on digital governance, technology regulation, data protection, or digital public infrastructure (like India Stack)",
                        "Participating universities include University of Passau, Willy Brandt School Erfurt, and Osnabrück"
                ),
                List.of("Germany", "DAAD", "Public Policy", "Digital Governance", "Full-Ride")
        ));

        // DAAD Master Studies for All Academic Disciplines
        curatedCatalog.add(new ScholarshipOpportunity(
                "DAAD_STUDY_SCHOLARSHIP",
                "DAAD Master Studies Scholarship for All Academic Disciplines",
                "German Academic Exchange Service (DAAD)",
                "Germany",
                "GERMANY",
                "FULL_RIDE",
                30000.0,
                "€934/month living stipend for 10–24 months + travel subsidy + health insurance + €460/yr annual study grant",
                8.5,
                0,
                true,
                null,
                90,
                6.5,
                null,
                "October – November (annually for the following Winter semester)",
                "Online submission via the DAAD Portal (Regional Branch Office New Delhi)",
                "https://www.daad.in/en/find-funding/scholarship-database/",
                List.of(
                        "First academic degree completed at a recognized Indian university (AICTE/UGC)",
                        "Outstanding academic performance (CGPA >= 8.5/10 or equivalent first-class distinction)",
                        "Detailed study proposal and well-argued choice of German Master's degree programs"
                ),
                List.of(
                        "Indian applicants apply through DAAD Regional Office New Delhi",
                        "APS certificate is mandatory for visa issuance upon receiving DAAD confirmation"
                ),
                List.of("Germany", "DAAD", "STEM", "Full-Ride", "Merit-Based")
        ));

        // Deutschlandstipendium
        curatedCatalog.add(new ScholarshipOpportunity(
                "DEUTSCHLANDSTIPENDIUM",
                "Deutschlandstipendium (German National Merit Scholarship)",
                "Federal Ministry of Education & Research (BMBF) & University Sponsors",
                "Germany",
                "GERMANY",
                "MONTHLY_STIPEND",
                4000.0,
                "€300/month tax-free cash stipend for at least two semesters, granted regardless of personal income",
                8.0,
                0,
                false,
                null,
                null,
                null,
                null,
                "June – September (set by each individual German university)",
                "Apply directly through your admitted German university portal (TUM, RWTH Aachen, LMU, TU Berlin)",
                "https://www.deutschlandstipendium.de/deutschlandstipendium/en/",
                List.of(
                        "Enrolled or admitted to a degree program at a participating German university",
                        "High academic grades (top 10–15% percentile or CGPA >= 8.0/10)",
                        "Extracurricular engagement, social commitment, or overcoming biographical hardship"
                ),
                List.of(
                        "Funded 50% by the Federal Government and 50% by enterprise sponsors (Siemens, BMW, SAP)",
                        "Does not count toward income limits and can be held concurrently with blocked account funds"
                ),
                List.of("Germany", "Deutschlandstipendium", "Merit", "Monthly-Stipend", "University-Level")
        ));

        // ─────────────────────────────────────────────────────────────────────
        // 2. JAPAN
        // ─────────────────────────────────────────────────────────────────────

        // MEXT Embassy Recommendation
        curatedCatalog.add(new ScholarshipOpportunity(
                "MEXT_EMBASSY",
                "MEXT Japanese Government Scholarship (Embassy Recommendation)",
                "Ministry of Education, Culture, Sports, Science and Technology (MEXT, Japan)",
                "Japan",
                "JAPAN",
                "FULL_RIDE",
                46000.0,
                "100% Full Tuition Exemption + ¥144,000–¥147,000/month living stipend + Round-trip international flight ticket from India to Japan",
                8.2,
                0,
                true,
                null,
                85,
                6.5,
                "N5 preferred (English tracks accept IELTS/TOEFL)",
                "April – May (annually for intake in October of following year)",
                "Direct application via the Embassy of Japan in New Delhi or regional Consulates (Mumbai/Bengaluru/Chennai/Kolkata)",
                "https://www.in.emb-japan.go.jp/itpr_en/education_study_in_japan.html",
                List.of(
                        "Indian national under 35 years of age with Bachelor's degree (16 years of education)",
                        "Minimum 65–70% marks or CGPA >= 8.2/10 in undergraduate engineering/science",
                        "Compelling and rigorous research proposal aligned with Japanese laboratory research themes",
                        "Passing the Embassy written exam (English & Japanese) and preliminary interview"
                ),
                List.of(
                        "The primary selection occurs at the Embassy in India; successful candidates then request provisional acceptance letters (Naidaku) from professors",
                        "Includes 6 months of paid Japanese language immersion prior to Master's enrollment"
                ),
                List.of("Japan", "MEXT", "Embassy-Track", "Full-Ride", "Airfare-Included", "Naidaku")
        ));

        // MEXT University Recommendation (Naidaku Track)
        curatedCatalog.add(new ScholarshipOpportunity(
                "MEXT_UNIVERSITY",
                "MEXT Japanese Government Scholarship (University Recommendation)",
                "Ministry of Education, Culture, Sports, Science and Technology (MEXT, Japan)",
                "Japan",
                "JAPAN",
                "FULL_RIDE",
                45000.0,
                "100% Full Tuition Waiver + ¥144,000/month living stipend + Round-trip airfare from India to Japan",
                8.0,
                0,
                true,
                null,
                80,
                6.5,
                "English-track suitable (JLPT is a bonus)",
                "November – January (annually for following October intake)",
                "Nomination submitted directly by the Japanese prospective research supervisor/university to MEXT",
                "https://www.studyinjapan.go.jp/en/planning/scholarships/mext-scholarships/",
                List.of(
                        "Formal nomination by a Japanese national or public university professor (e.g. UTokyo, Kyoto, Tokyo Tech)",
                        "Candidate must have secured informal consent (Naidaku) from the laboratory professor",
                        "Undergraduate CGPA >= 8.0/10 or equivalent top-quartile performance",
                        "English proficiency certification (IELTS >= 6.5 or TOEFL >= 80) or medium of instruction certificate"
                ),
                List.of(
                        "Directly leverage our Faculty & Lab Alignment Critic to target labs with active MEXT university quota",
                        "Contact professors between August and October with your research proposal and CV"
                ),
                List.of("Japan", "MEXT", "University-Recommendation", "Naidaku", "Full-Ride")
        ));

        // JASSO Honors Scholarship
        curatedCatalog.add(new ScholarshipOpportunity(
                "JASSO_HONORS",
                "JASSO Monbukagakusho Honors Scholarship for Privately-Financed Students",
                "Japan Student Services Organization (JASSO)",
                "Japan",
                "JAPAN",
                "MONTHLY_STIPEND",
                4200.0,
                "¥48,000/month study grant for 12 months for enrolled privately-financed graduate students",
                7.0,
                0,
                false,
                null,
                null,
                null,
                null,
                "April – May (Spring enrollment) or October – November (Fall enrollment)",
                "Recommended and submitted through your admitted Japanese Graduate School international office",
                "https://www.jasso.go.jp/en/ryugaku/scholarships/study_j/scholarships/index.html",
                List.of(
                        "Privately-financed international student admitted to a Japanese graduate school",
                        "Undergraduate CGPA of 2.30/3.00 on the JASSO GPA scale (approx. >= 7.0/10)",
                        "Monthly allowance received from home sponsor does not exceed ¥90,000"
                ),
                List.of(
                        "High recipient allocation across national universities like Tokyo Tech, Tohoku, and Osaka",
                        "Can be renewed or stacked with tuition reduction waivers (up to 50% or 100% reduction)"
                ),
                List.of("Japan", "JASSO", "Living-Stipend", "Accessible-GPA", "Enrolled-Students")
        ));

        // ─────────────────────────────────────────────────────────────────────
        // 3. US & UK
        // ─────────────────────────────────────────────────────────────────────

        // Fulbright-Nehru Master's Fellowships
        curatedCatalog.add(new ScholarshipOpportunity(
                "FULBRIGHT_NEHRU",
                "Fulbright-Nehru Master's Fellowships",
                "United States-India Educational Foundation (USIEF)",
                "USA",
                "US_UK",
                "FULL_RIDE",
                90000.0,
                "100% J-1 Visa tuition coverage + monthly living stipend + round-trip economy airfare from India + health accident coverage",
                7.0,
                36,     // Minimum 3 years (36 months) of full-time professional work experience
                false,
                158,
                90,
                6.5,
                null,
                "Mid-May (annually, 15 months prior to US enrollment)",
                "Online application via the USIEF official Fulbright-Nehru Portal",
                "https://www.usief.org.in/Fulbright-Nehru-Fellowships.aspx",
                List.of(
                        "Indian citizen with equivalent of US 4-year Bachelor's degree (min 55% marks from recognized Indian institution)",
                        "Minimum 3 years (36 months) of full-time professional work experience after degree completion",
                        "Demonstrated leadership capabilities and record of community/social service",
                        "Mandatory J-1 visa requirement: must return to India for minimum 2 years upon graduation"
                ),
                List.of(
                        "Applications open in February and close in mid-May for programs starting the following year",
                        "Focus personal statements on community impact and how your MS in the US benefits Indian institutions"
                ),
                List.of("USA", "Fulbright", "USIEF", "Work-Experience", "Full-Ride", "Leadership")
        ));

        // Inlaks Shivdasani Foundation Scholarships
        curatedCatalog.add(new ScholarshipOpportunity(
                "INLAKS_SHIVDASANI",
                "Inlaks Shivdasani Foundation Scholarships",
                "Inlaks Shivdasani Foundation",
                "USA & Europe",
                "US_UK",
                "FULL_RIDE",
                100000.0,
                "Up to $100,000 USD covering full tuition fees, university living expenses, one-way flight, and health insurance",
                8.5,
                0,
                true,
                164,
                105,
                7.5,
                null,
                "March 15 – March 30 (annually)",
                "Online application through Inlaks Foundation portal after securing admission to a top-tier global university",
                "https://www.inlaksfoundation.org/scholarships/",
                List.of(
                        "Indian passport holder holding an undergraduate degree from a recognized Indian university",
                        "Age under 30 on July 1 of the application year",
                        "Admission to a top-tier institution in North America or Europe (e.g. MIT, Stanford, Harvard, Oxford, Cambridge, Imperial, ETH)",
                        "Outstanding academic credentials (CGPA >= 8.5/10) with special achievements/honors"
                ),
                List.of(
                        "One of the largest private grants available to Indian students heading to world top-20 universities",
                        "Interviews are conducted in New Delhi and Mumbai by a distinguished selection panel"
                ),
                List.of("USA", "UK", "Inlaks", "Top-Universities", "Full-Ride", "Merit")
        ));

        // US & UK Graduate Assistantship (TA / RA) Fee Remission
        curatedCatalog.add(new ScholarshipOpportunity(
                "US_UK_GRADUATE_TARA",
                "Graduate Teaching & Research Assistantship (TA/RA) Fee Remission",
                "US & UK University Academic Departments",
                "USA & UK",
                "US_UK",
                "ASSISTANTSHIP",
                58000.0,
                "100% Tuition Waiver ($25,000–$55,000/yr) + Monthly living stipend ($1,800–$3,500/mo for 20 hrs/week) + Subsidized health insurance",
                8.0,
                0,
                true,
                164,    // GRE Quant 164+
                100,    // TOEFL 100+ (Speaking 24-26)
                7.5,    // IELTS 7.5 (Speaking 8.0)
                null,
                "Concurrent with graduate school admission deadlines (December – February)",
                "Indicate interest directly on university graduate application portal; follow up with faculty lab directors",
                "https://gradschool.edu/funding/assistantships",
                List.of(
                        "Admitted to a research-oriented MS/PhD program at an accredited US or UK university",
                        "High GRE Quantitative score (164+) and strong foundation in core CS subjects",
                        "Spoken English certification for Teaching Assistants (TOEFL Speaking >= 26 or IELTS Speaking >= 8.0)",
                        "Research background or thesis required for Research Assistantships (RA)"
                ),
                List.of(
                        "50% FTE (20 hrs/week) provides full 100% in-state/out-of-state tuition remission",
                        "Reach out to faculty with aligned research interests before offer decisions are finalized in March"
                ),
                List.of("USA", "UK", "Assistantship", "TA-RA", "Tuition-Remission", "Stipend", "F1-Visa")
        ));

        // UK Chevening Scholarship
        curatedCatalog.add(new ScholarshipOpportunity(
                "UK_CHEVENING",
                "UK Chevening Master's Scholarship",
                "Foreign, Commonwealth & Development Office (UK Government)",
                "UK",
                "US_UK",
                "FULL_RIDE",
                48000.0,
                "100% University tuition fees + Monthly living allowance (£1,200–£1,500/mo) + Return flights to UK + Visa application fee",
                7.5,
                24,     // Minimum 2 years (2,800 hours) work experience
                false,
                null,
                90,
                6.5,
                null,
                "August – November (annually, for following Autumn intake)",
                "Online application through the global Chevening application portal",
                "https://www.chevening.org/scholarship/india/",
                List.of(
                        "Citizen of a Chevening-eligible country (India is the largest recipient globally)",
                        "Undergraduate degree equivalent to an upper second-class 2:1 honours degree (min 7.0–7.5/10)",
                        "At least two years (equivalent to 2,800 hours) of demonstrable work experience/internships",
                        "Apply to three eligible UK master's programs and receive an unconditional offer"
                ),
                List.of(
                        "India holds the largest Chevening programme in the world with over 50 scholarships annually",
                        "Requires returning to country of citizenship for minimum two years following degree completion"
                ),
                List.of("UK", "Chevening", "Full-Ride", "Leadership", "Work-Experience")
        ));

        // ─────────────────────────────────────────────────────────────────────
        // 4. EUROPE (Erasmus Mundus & Merit Grants)
        // ─────────────────────────────────────────────────────────────────────

        // Erasmus Mundus Joint Masters (EMJM)
        curatedCatalog.add(new ScholarshipOpportunity(
                "ERASMUS_MUNDUS_EMJM",
                "Erasmus Mundus Joint Masters (EMJM) Full-Ride Scholarship",
                "European Commission (European Union)",
                "Europe (Multi-Country)",
                "EUROPE",
                "FULL_RIDE",
                52000.0,
                "100% Full Tuition Coverage for 24 months + €1,400/month living allowance + €3,000/yr travel & installation grant + full health insurance",
                8.0,
                0,
                true,
                null,
                90,
                6.5,
                null,
                "October – January (annually, varies by master consortium)",
                "Direct application to individual Erasmus Mundus consortia (e.g., BDMA, SECCLO, MERIT, COSI)",
                "https://www.eacea.ec.europa.eu/scholarships/erasmus-mundus-catalogue_en",
                List.of(
                        "Bachelor's degree in CS, Computer Engineering, Math, or related STEM field (CGPA >= 8.0/10)",
                        "Strong statement of motivation matching the multi-country university curriculum",
                        "English proficiency (IELTS 6.5+ or TOEFL 90+)",
                        "Open to Indian candidates under EU 'Partner Country' quota (India frequently wins the #1 highest number of EMJM awards worldwide)"
                ),
                List.of(
                        "Allows students to study across at least 2 distinct European countries and graduate with joint/multiple degrees",
                        "Each student can apply to up to three distinct EMJM programs in a single admission round"
                ),
                List.of("Europe", "Erasmus-Mundus", "EMJM", "Full-Ride", "Multi-Country", "Joint-Degree")
        ));

        // ETH Zurich Excellence Scholarship (ESOP)
        curatedCatalog.add(new ScholarshipOpportunity(
                "ETH_ESOP",
                "ETH Excellence Scholarship & Opportunity Programme (ESOP)",
                "ETH Zurich Foundation",
                "Switzerland",
                "EUROPE",
                "FULL_RIDE",
                54000.0,
                "Full study fee waiver + CHF 12,000/semester living & study grant (CHF 48,000 total) + faculty mentoring",
                9.0,    // Top 10%
                0,
                true,   // Bachelor Thesis required
                166,    // High GRE Quant recommended
                100,
                7.5,
                null,
                "November 1 – December 15 (annually)",
                "Combined application for Master's admission and ESOP via the ETH Zurich e-Apply portal",
                "https://ethz.ch/students/en/studies/financial/scholarships/excellencescholarship.html",
                List.of(
                        "Outstanding undergraduate academic record: Grade A or top 10% of bachelor's degree class (CGPA >= 9.0/10)",
                        "Bachelor's thesis completed or documented extensive individual research project",
                        "High score on GRE Quantitative section (166+ recommended for engineering applicants)"
                ),
                List.of(
                        "Requires a formal pre-proposal for your Master's thesis alongside the application",
                        "Mentorship by an ETH professor and access to the exclusive ETH Foundation scholar network"
                ),
                List.of("Switzerland", "ETH-Zurich", "ESOP", "High-Merit", "Research", "Full-Ride")
        ));

        // Italian DSU Regional Scholarship
        curatedCatalog.add(new ScholarshipOpportunity(
                "ITALY_DSU",
                "Italian Regional DSU Scholarship (Diritto allo Studio Universitario)",
                "Regional Governments of Lombardy / Lazio / Piedmont (Italy)",
                "Italy",
                "EUROPE",
                "FULL_RIDE",
                20000.0,
                "100% Tuition Fee Waiver + Free Campus Canteen Meals + up to €7,200/yr direct cash stipend for living expenses",
                6.5,
                0,
                false,
                null,
                null,
                6.0,
                null,
                "July – September (annually)",
                "Online application to regional agencies (e.g., DSU Politecnico di Milano, Disco Lazio) with Indian family income documentation (ISEEE Parificato)",
                "https://www.polimi.it/en/international-prospective-students/fees-and-scholarships/dsu-scholarships",
                List.of(
                        "Admitted to an Italian public university (Politecnico di Milano, Sapienza, UniTo, PoliTo)",
                        "Equivalent family income index (ISEEE Parificato) under €25,000/year (most middle-class Indian families qualify)",
                        "Requires legalization/apostille of parent income tax returns and property records via Italian Embassy/Consulate in India"
                ),
                List.of(
                        "One of the most accessible funding opportunities in Europe for Indian students admitted to top Italian polytechnics",
                        "Begin legalizing family income documents with the Italian consulate in Mumbai/New Delhi early in June"
                ),
                List.of("Italy", "DSU", "Politecnico-di-Milano", "Tuition-Waiver", "Living-Stipend", "Accessible-GPA")
        ));

        // French Eiffel Excellence Scholarship
        curatedCatalog.add(new ScholarshipOpportunity(
                "CAMPUS_FRANCE_EIFFEL",
                "Eiffel Excellence Scholarship (Campus France)",
                "Ministry for Europe and Foreign Affairs (France)",
                "France",
                "EUROPE",
                "FULL_RIDE",
                32000.0,
                "€1,181/month living allowance for up to 24 months + return international flight from India + health insurance + cultural excursions",
                8.5,
                0,
                true,
                null,
                90,
                6.5,
                null,
                "October – December (annually, institution-specific)",
                "Candidate cannot apply directly; French higher education institutions (e.g. IP Paris, Sorbonne) select and nominate candidates",
                "https://www.campusfrance.org/en/eiffel-scholarship-program-of-excellence",
                List.of(
                        "Non-French national under 25 years old at the committee evaluation date",
                        "Outstanding academic record (top 10% of graduating engineering class)",
                        "Pre-selected and submitted by an accredited French Grand École or university"
                ),
                List.of(
                        "Apply to French institutions early in September/October to request Eiffel nomination before their institutional quota closes",
                        "Tuition fees at public French universities are separately covered or waived"
                ),
                List.of("France", "Eiffel", "Campus-France", "Full-Ride", "Engineering")
        ));

        // Portuguese FCT Master Research Fellowship
        curatedCatalog.add(new ScholarshipOpportunity(
                "PORTUGAL_FCT",
                "FCT Master Research Fellowship (Bolsa de Investigação para Mestre)",
                "Fundação para a Ciência e a Tecnologia (FCT, Portugal)",
                "Portugal",
                "EUROPE",
                "TUITION_PLUS_STIPEND",
                24000.0,
                "100% University tuition remission + €990/month living stipend + social security coverage for research scholars",
                8.0,
                0,
                true,
                null,
                80,
                6.5,
                null,
                "Rolling applications through associated research institutes (INESC-ID, ITI, ISR Lisbon)",
                "Apply directly to accredited research laboratories at Instituto Superior Técnico (IST) or University of Porto",
                "https://www.fct.pt/apoios/bolsas/",
                List.of(
                        "Admitted or enrolled in a Master's degree at a Portuguese university (IST Lisbon, UP)",
                        "Strong background in CS, Artificial Intelligence, Robotics, or Systems Engineering",
                        "Commitment to 20–30 hours/week of research laboratory participation"
                ),
                List.of(
                        "Portugal offers low cost of living, making a €990/mo stipend comfortable",
                        "Great stepping stone for European research or tech startups in Lisbon's thriving hub"
                ),
                List.of("Portugal", "FCT", "Research-Fellowship", "IST-Lisbon", "Stipend")
        ));
    }
}
