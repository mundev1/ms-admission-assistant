package com.admissions.engine.service;

import com.admissions.engine.model.ects.*;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Service orchestrating German ECTS credit equivalence conversion, syllabus mapping,
 * prerequisite deficit analysis, and bridge course recommendation for Indian applicants.
 */
@Service
public class EctsPreScreenerService {

    private static final DecimalFormat ONE_DECIMAL = new DecimalFormat("#0.0");

    private final List<UniversityEctsRequirement> universityPresets = new ArrayList<>();
    private final List<CurriculumSample> sampleCurricula = new ArrayList<>();
    private final List<BridgeCourseRecommendation> bridgeCatalog = new ArrayList<>();

    public EctsPreScreenerService() {
        initializeUniversityPresets();
        initializeSampleCurricula();
        initializeBridgeCatalog();
    }

    /**
     * Returns all configured German university prerequisite requirements.
     */
    public List<UniversityEctsRequirement> getUniversityPresets() {
        return Collections.unmodifiableList(universityPresets);
    }

    /**
     * Returns all pre-loaded sample Indian university curricula.
     */
    public List<CurriculumSample> getSampleCurricula() {
        return Collections.unmodifiableList(sampleCurricula);
    }

    /**
     * Returns all accredited bridge courses and MOOC recommendations.
     */
    public List<BridgeCourseRecommendation> getBridgeCatalog() {
        return Collections.unmodifiableList(bridgeCatalog);
    }

    /**
     * Evaluates a student's undergraduate curriculum against the selected German university's entry regulations.
     */
    public EctsEvaluationResult evaluateCurriculum(EctsEvaluationRequest request) {
        UniversityEctsRequirement req = resolveRequirement(request);
        String mode = request.getConversionMode() != null ? request.getConversionMode() : "STANDARD_AICTE";
        double hoursPerEcts = request.getHoursPerEcts() > 0 ? request.getHoursPerEcts() : 28.0;

        List<SubjectEntry> subjects = request.getSubjects() != null ? request.getSubjects() : new ArrayList<>();

        // 1. Convert each subject to ECTS according to the conversion mode
        double totalEarnedIndianCredits = 0.0;
        double totalEarnedEcts = 0.0;

        Map<EctsCategory, Double> categoryIndianCredits = new EnumMap<>(EctsCategory.class);
        Map<EctsCategory, Double> categoryEcts = new EnumMap<>(EctsCategory.class);
        Map<EctsCategory, List<String>> categorySubjectNames = new EnumMap<>(EctsCategory.class);

        for (EctsCategory cat : EctsCategory.values()) {
            categoryIndianCredits.put(cat, 0.0);
            categoryEcts.put(cat, 0.0);
            categorySubjectNames.put(cat, new ArrayList<>());
        }

        for (SubjectEntry subj : subjects) {
            double subjectEcts = calculateSubjectEcts(subj, mode, hoursPerEcts);
            subj.setCalculatedEcts(roundOne(subjectEcts));

            EctsCategory cat = subj.getCategory() != null ? subj.getCategory() : EctsCategory.ELECTIVES_OTHER;
            categoryIndianCredits.put(cat, categoryIndianCredits.get(cat) + subj.getIndianCredits());
            categoryEcts.put(cat, categoryEcts.get(cat) + subjectEcts);
            categorySubjectNames.get(cat).add(subj.getName() + " (" + roundOne(subjectEcts) + " ECTS)");

            totalEarnedIndianCredits += subj.getIndianCredits();
            totalEarnedEcts += subjectEcts;
        }

        totalEarnedIndianCredits = roundOne(totalEarnedIndianCredits);
        totalEarnedEcts = roundOne(totalEarnedEcts);

        // 2. Perform Category Credit Analysis
        Map<EctsCategory, CategoryCreditAnalysis> analyses = new EnumMap<>(EctsCategory.class);
        List<String> criticalDeficits = new ArrayList<>();
        List<BridgeCourseRecommendation> recommendedBridgeCourses = new ArrayList<>();

        double theoEarned = roundOne(categoryEcts.get(EctsCategory.THEORETICAL_CS));
        double theoReq = req.getMinTheoreticalCsEcts();
        analyses.put(EctsCategory.THEORETICAL_CS, buildAnalysis(
            EctsCategory.THEORETICAL_CS,
            roundOne(categoryIndianCredits.get(EctsCategory.THEORETICAL_CS)),
            theoEarned,
            theoReq,
            categorySubjectNames.get(EctsCategory.THEORETICAL_CS),
            criticalDeficits
        ));

        double mathEarned = roundOne(categoryEcts.get(EctsCategory.MATHEMATICS));
        double mathReq = req.getMinMathematicsEcts();
        analyses.put(EctsCategory.MATHEMATICS, buildAnalysis(
            EctsCategory.MATHEMATICS,
            roundOne(categoryIndianCredits.get(EctsCategory.MATHEMATICS)),
            mathEarned,
            mathReq,
            categorySubjectNames.get(EctsCategory.MATHEMATICS),
            criticalDeficits
        ));

        double sysEarned = roundOne(categoryEcts.get(EctsCategory.PRACTICAL_SYSTEMS_CS));
        double sysReq = req.getMinPracticalSystemsEcts();
        analyses.put(EctsCategory.PRACTICAL_SYSTEMS_CS, buildAnalysis(
            EctsCategory.PRACTICAL_SYSTEMS_CS,
            roundOne(categoryIndianCredits.get(EctsCategory.PRACTICAL_SYSTEMS_CS)),
            sysEarned,
            sysReq,
            categorySubjectNames.get(EctsCategory.PRACTICAL_SYSTEMS_CS),
            criticalDeficits
        ));

        double seEarned = roundOne(categoryEcts.get(EctsCategory.SOFTWARE_ENGINEERING));
        double seReq = req.getMinSoftwareEngEcts();
        analyses.put(EctsCategory.SOFTWARE_ENGINEERING, buildAnalysis(
            EctsCategory.SOFTWARE_ENGINEERING,
            roundOne(categoryIndianCredits.get(EctsCategory.SOFTWARE_ENGINEERING)),
            seEarned,
            seReq,
            categorySubjectNames.get(EctsCategory.SOFTWARE_ENGINEERING),
            criticalDeficits
        ));

        double elecEarned = roundOne(categoryEcts.get(EctsCategory.ELECTIVES_OTHER));
        analyses.put(EctsCategory.ELECTIVES_OTHER, new CategoryCreditAnalysis(
            EctsCategory.ELECTIVES_OTHER,
            roundOne(categoryIndianCredits.get(EctsCategory.ELECTIVES_OTHER)),
            elecEarned,
            0.0,
            0.0,
            1.0,
            "SATISFIED",
            categorySubjectNames.get(EctsCategory.ELECTIVES_OTHER),
            "Contributes to overall bachelor credit tally (" + elecEarned + " ECTS earned)."
        ));

        // 3. Determine Overall Verdict
        double maxDeficit = req.getMaxAllowableDeficitEcts();
        double totalDeficit = 0.0;
        for (CategoryCreditAnalysis cca : analyses.values()) {
            totalDeficit += cca.getDeficitEcts();
        }

        String overallVerdict;
        if (totalDeficit <= 0.01) {
            overallVerdict = "ADMISSION_ELIGIBLE";
        } else if (totalDeficit <= maxDeficit && analyses.get(EctsCategory.THEORETICAL_CS).getDeficitEcts() <= 4.0) {
            overallVerdict = "CONDITIONAL_ADMISSION_RISK";
        } else {
            overallVerdict = "HIGH_REJECTION_RISK";
        }

        // 4. Attach targeted Bridge Courses for categories with deficits
        for (CategoryCreditAnalysis cca : analyses.values()) {
            if (cca.getDeficitEcts() > 0) {
                for (BridgeCourseRecommendation bcr : bridgeCatalog) {
                    if (bcr.getCategory() == cca.getCategory()) {
                        recommendedBridgeCourses.add(bcr);
                    }
                }
            }
        }

        // 5. Generate Markdown Curriculum Analysis Table (Formblatt)
        String markdownTable = generateCurriculumTableMarkdown(req, analyses, totalEarnedIndianCredits, totalEarnedEcts);

        // 6. Generate Executive Summary & Auflagen Advisory
        String executiveSummary = generateSummary(req, overallVerdict, totalEarnedEcts, totalDeficit, analyses);
        String auflagenAdvisory = generateAuflagenAdvisory(req, overallVerdict, totalDeficit);

        double totalRequired = req.getMinTotalCsEcts() > 0 ? req.getMinTotalCsEcts() : 110.0;
        double overallPct = roundOne(Math.min(100.0, (totalEarnedEcts / totalRequired) * 100.0));

        return new EctsEvaluationResult(
            req.getId(),
            req.getUniversityName(),
            req.getProgramName(),
            totalEarnedIndianCredits,
            totalEarnedEcts,
            totalRequired,
            overallPct,
            overallVerdict,
            executiveSummary,
            auflagenAdvisory,
            analyses,
            criticalDeficits,
            recommendedBridgeCourses,
            markdownTable
        );
    }

    // ── Helper Calculation Methods ────────────────────────────────────────────

    private double calculateSubjectEcts(SubjectEntry subj, String mode, double hoursPerEcts) {
        double credits = subj.getIndianCredits() > 0 ? subj.getIndianCredits() : 3.0;

        switch (mode.toUpperCase()) {
            case "WORKLOAD_HOURS":
                // 15 weeks per semester in Indian universities
                // Lecture: 1 hr * 15 weeks = 15 contact hrs + 15 self-study = 30 hrs
                // Tutorial: 1 hr * 15 weeks = 15 contact hrs + 15 self-study = 30 hrs
                // Practical: 2 hrs * 15 weeks = 30 contact hrs + 10 lab prep = 40 hrs
                int l = subj.getLectureHours() > 0 ? subj.getLectureHours() : (int) Math.round(credits);
                int t = subj.getTutorialHours();
                int p = subj.getPracticalHours();
                double totalHours = (l * 30.0) + (t * 30.0) + (p * 20.0);
                if (totalHours <= 0) totalHours = credits * 38.0;
                return totalHours / hoursPerEcts;

            case "RATIO_1_33":
                return credits * 1.3333;

            case "RATIO_1_5":
                return credits * 1.5;

            case "STANDARD_AICTE":
            default:
                // AICTE 160 credits over 4 years = 224 to 240 ECTS (factor 1.40)
                return credits * 1.40;
        }
    }

    private CategoryCreditAnalysis buildAnalysis(EctsCategory cat, double earnedIndian, double earnedEcts,
                                                 double requiredEcts, List<String> subjects, List<String> criticalDeficits) {
        double deficit = roundOne(Math.max(0.0, requiredEcts - earnedEcts));
        double ratio = requiredEcts > 0 ? roundOne(Math.min(1.0, earnedEcts / requiredEcts)) : 1.0;

        String status;
        String msg;

        if (deficit <= 0.01) {
            status = "SATISFIED";
            msg = "Exceeds strict prerequisite requirement (" + earnedEcts + " ECTS earned vs " + requiredEcts + " ECTS required).";
        } else if (deficit <= 5.0) {
            status = "CONDITIONAL_GAP";
            msg = "Shortfall of " + deficit + " ECTS. University may issue conditional admission (\"Auflagen\") to complete this bachelor exam during Semester 1/2.";
            criticalDeficits.add(cat.getDisplayName() + ": Shortfall of " + deficit + " ECTS (" + earnedEcts + " / " + requiredEcts + " ECTS).");
        } else {
            status = "CRITICAL_DEFICIT";
            msg = "⚠️ CRITICAL REJECTION BOTTLENECK: Shortfall of " + deficit + " ECTS. German admission committees will formally reject this application before GPA screening.";
            criticalDeficits.add("CRITICAL DEFICIT: " + cat.getDisplayName() + " lacks " + deficit + " ECTS (" + earnedEcts + " earned, " + requiredEcts + " required). Immediate bridge coursework required.");
        }

        return new CategoryCreditAnalysis(
            cat, earnedIndian, earnedEcts, requiredEcts, deficit, ratio, status, subjects, msg
        );
    }

    private String generateSummary(UniversityEctsRequirement req, String verdict,
                                   double totalEarned, double totalDeficit,
                                   Map<EctsCategory, CategoryCreditAnalysis> analyses) {
        StringBuilder sb = new StringBuilder();
        sb.append("Pre-screening assessment for ").append(req.getUniversityName()).append(" (")
          .append(req.getProgramName()).append("): ");

        if ("ADMISSION_ELIGIBLE".equals(verdict)) {
            sb.append("✅ Fully Eligible on Curriculum Equivalence! Your undergraduate syllabus covers ")
              .append(totalEarned).append(" ECTS and meets all departmental minimum thresholds in Theoretical CS, Mathematics, and Practical Systems.");
        } else if ("CONDITIONAL_ADMISSION_RISK".equals(verdict)) {
            sb.append("⚠️ Conditional Admission Feasible with Auflagen. You have an aggregate deficit of ")
              .append(roundOne(totalDeficit)).append(" ECTS, which is within ").append(req.getUniversityName())
              .append("'s allowable bridge threshold (≤ ").append(req.getMaxAllowableDeficitEcts())
              .append(" ECTS). The faculty may require you to pass 1–2 bachelor bridge exams in Munich/Aachen.");
        } else {
            sb.append("🚨 HIGH REJECTION RISK: Your syllabus fails the strict German prerequisite check with a gap of ")
              .append(roundOne(totalDeficit)).append(" ECTS. In Germany, Master's admission is legally consecutive—German law forbids universities from admitting candidates lacking essential bachelor prerequisites.");
        }
        return sb.toString();
    }

    private String generateAuflagenAdvisory(UniversityEctsRequirement req, String verdict, double totalDeficit) {
        if ("ADMISSION_ELIGIBLE".equals(verdict)) {
            return "No Auflagen expected. Direct admission to Master's coursework without remedial exams.";
        } else if ("CONDITIONAL_ADMISSION_RISK".equals(verdict)) {
            return "German Auflagen Policy: Under Bavarian and NRW Higher Education Acts, faculties can attach up to 30 ECTS of bachelor bridge exams (Auflagen). You must clear these exams within 2 semesters while enrolled.";
        } else {
            return "Hard Deficit Warning: Deficit exceeds allowable Auflagen limit (" + roundOne(totalDeficit) + " ECTS vs maximum " + req.getMaxAllowableDeficitEcts() + " ECTS). Take accredited NPTEL / edX courses before applying to prevent direct administrative rejection.";
        }
    }

    private String generateCurriculumTableMarkdown(UniversityEctsRequirement req,
                                                  Map<EctsCategory, CategoryCreditAnalysis> analyses,
                                                  double totalIndian, double totalEcts) {
        StringBuilder sb = new StringBuilder();
        sb.append("### German University Curriculum Equivalence Analysis (Formblatt)\n\n");
        sb.append("**Target Institution:** ").append(req.getUniversityName()).append(" — ").append(req.getProgramName()).append("\n");
        sb.append("**Total Completed Indian Credits:** ").append(totalIndian).append(" Credits (~").append(totalEcts).append(" ECTS Equivalent)\n\n");
        sb.append("| Subject Domain | Indian Credits | Calculated ECTS | Departmental Requirement | Deficit / Surplus | Status |\n");
        sb.append("| :--- | :---: | :---: | :---: | :---: | :--- |\n");

        for (EctsCategory cat : EctsCategory.values()) {
            CategoryCreditAnalysis cca = analyses.get(cat);
            if (cca == null) continue;
            String diffStr = cca.getDeficitEcts() > 0 ? ("-" + cca.getDeficitEcts() + " ECTS") : ("+" + roundOne(cca.getEarnedEcts() - cca.getRequiredEcts()) + " ECTS");
            String statusBadge = "SATISFIED".equals(cca.getStatus()) ? "🟢 MET" : ("CONDITIONAL_GAP".equals(cca.getStatus()) ? "🟡 MINOR GAP" : "🔴 HARD GAP");
            sb.append("| **").append(cat.getDisplayName()).append("** | ")
              .append(cca.getEarnedIndianCredits()).append(" | ")
              .append(cca.getEarnedEcts()).append(" ECTS | ")
              .append(cca.getRequiredEcts()).append(" ECTS | ")
              .append(diffStr).append(" | ")
              .append(statusBadge).append(" |\n");
        }
        return sb.toString();
    }

    private UniversityEctsRequirement resolveRequirement(EctsEvaluationRequest req) {
        if (req.getCustomRequirement() != null) {
            return req.getCustomRequirement();
        }
        String targetId = req.getTargetUniversityId() != null ? req.getTargetUniversityId().trim().toUpperCase() : "TUM";
        for (UniversityEctsRequirement r : universityPresets) {
            if (r.getId().equalsIgnoreCase(targetId) || targetId.contains(r.getId().toUpperCase())) {
                return r;
            }
        }
        return universityPresets.get(0); // TUM default
    }

    private double roundOne(double val) {
        return Math.round(val * 10.0) / 10.0;
    }

    // ── Catalog Initializers ──────────────────────────────────────────────────

    private void initializeUniversityPresets() {
        universityPresets.add(new UniversityEctsRequirement(
            "TUM",
            "Technical University of Munich (TUM)",
            "MSc Informatics / Data Engineering & Analytics",
            "Bavaria",
            28.0, // Theoretical CS
            18.0, // Mathematics
            32.0, // Practical Systems CS
            14.0, // Software Engineering
            120.0, // Total CS
            6.0,  // Max deficit for Auflagen
            "Strict consecutive admission: Under TUM regulations, applicants must demonstrate strictly ≥ 28 ECTS in Theoretical CS (Automata, Complexity, Formal Languages, Algorithms). Applicants with > 6 ECTS deficit are rejected outright.",
            List.of("Theory of Computation", "Algorithms & Data Structures", "Discrete Structures", "Operating Systems", "Computer Architecture")
        ));

        universityPresets.add(new UniversityEctsRequirement(
            "RWTH_AACHEN",
            "RWTH Aachen University",
            "MSc Computer Science (Informatik)",
            "North Rhine-Westphalia",
            18.0, // Theoretical CS
            18.0, // Mathematics
            30.0, // Practical Systems CS
            14.0, // Software Engineering
            110.0, // Total CS
            8.0,  // Max deficit
            "RWTH Aachen requires 18 ECTS in Theoretical Computer Science (Formal Systems, Logic, Complexity) and 18 ECTS in Mathematics. Missing credits can be assigned as Auflagen up to 8 ECTS.",
            List.of("Formal Languages & Automata", "Computability & Complexity", "Discrete Mathematics", "Operating Systems", "Databases")
        ));

        universityPresets.add(new UniversityEctsRequirement(
            "STUTTGART",
            "University of Stuttgart",
            "MSc Computer Science",
            "Baden-Württemberg",
            12.0, // Theoretical CS
            18.0, // Mathematics
            36.0, // Practical Systems CS
            12.0, // Software Engineering
            100.0, // Total CS
            12.0, // Max deficit
            "University of Stuttgart allows slightly higher flexibility in theoretical CS but enforces strictly 18 ECTS in Higher Mathematics and 36 ECTS in practical systems.",
            List.of("Theoretical Computer Science", "Calculus & Linear Algebra", "Distributed Systems", "Software Engineering")
        ));

        universityPresets.add(new UniversityEctsRequirement(
            "KIT",
            "Karlsruhe Institute of Technology (KIT)",
            "MSc Informatics",
            "Baden-Württemberg",
            20.0, // Theoretical CS
            22.0, // Mathematics
            30.0, // Practical Systems CS
            12.0, // Software Engineering
            110.0, // Total CS
            8.0,  // Max deficit
            "KIT has high quantitative standards requiring 22 ECTS in Mathematics & Statistics and 20 ECTS in Theoretical Informatics.",
            List.of("Theoretical Foundations of CS", "Algorithms I & II", "Linear Algebra I & II", "Analysis I & II")
        ));

        universityPresets.add(new UniversityEctsRequirement(
            "TU_BERLIN",
            "Technical University of Berlin",
            "MSc Computer Science",
            "Berlin",
            14.0, // Theoretical CS
            18.0, // Mathematics
            34.0, // Practical Systems CS
            12.0, // Software Engineering
            105.0, // Total CS
            10.0, // Max deficit
            "TU Berlin requires 18 ECTS in Mathematics and 14 ECTS in Theoretical Foundations with an overall 105 ECTS in Computer Science.",
            List.of("Theoretical Foundations", "Discrete Structures", "Computer Architecture", "Software Engineering")
        ));
    }

    private void initializeSampleCurricula() {
        // 1. AICTE B.Tech CSE Model Curriculum (Full 4-Year)
        List<SubjectEntry> aicte = new ArrayList<>();
        aicte.add(new SubjectEntry("S101", "Engineering Mathematics I (Calculus & Linear Algebra)", EctsCategory.MATHEMATICS, 4.0, 3, 1, 0, "A", 5.6, 1));
        aicte.add(new SubjectEntry("S102", "Programming for Problem Solving with C", EctsCategory.SOFTWARE_ENGINEERING, 4.0, 3, 0, 2, "A+", 5.6, 1));
        aicte.add(new SubjectEntry("S201", "Engineering Mathematics II (Multivariable Calculus & ODE)", EctsCategory.MATHEMATICS, 4.0, 3, 1, 0, "A", 5.6, 2));
        aicte.add(new SubjectEntry("S202", "Digital Electronics & Logic Design", EctsCategory.PRACTICAL_SYSTEMS_CS, 4.0, 3, 0, 2, "B+", 5.6, 2));
        aicte.add(new SubjectEntry("S301", "Discrete Mathematics & Graph Structures", EctsCategory.MATHEMATICS, 4.0, 3, 1, 0, "A", 5.6, 3));
        aicte.add(new SubjectEntry("S302", "Data Structures & Algorithms", EctsCategory.THEORETICAL_CS, 4.0, 3, 0, 2, "A+", 5.6, 3));
        aicte.add(new SubjectEntry("S303", "Object-Oriented Programming (Java/C++)", EctsCategory.SOFTWARE_ENGINEERING, 4.0, 3, 0, 2, "A", 5.6, 3));
        aicte.add(new SubjectEntry("S304", "Computer Organization & Architecture", EctsCategory.PRACTICAL_SYSTEMS_CS, 4.0, 3, 1, 0, "B+", 5.6, 3));
        aicte.add(new SubjectEntry("S401", "Design & Analysis of Algorithms", EctsCategory.THEORETICAL_CS, 4.0, 3, 1, 0, "A", 5.6, 4));
        aicte.add(new SubjectEntry("S402", "Operating Systems & Concurrency", EctsCategory.PRACTICAL_SYSTEMS_CS, 4.0, 3, 0, 2, "A", 5.6, 4));
        aicte.add(new SubjectEntry("S403", "Probability, Statistics & Stochastic Processes", EctsCategory.MATHEMATICS, 3.0, 3, 0, 0, "A", 4.2, 4));
        aicte.add(new SubjectEntry("S501", "Theory of Computation & Automata", EctsCategory.THEORETICAL_CS, 4.0, 3, 1, 0, "B+", 5.6, 5));
        aicte.add(new SubjectEntry("S502", "Database Management Systems", EctsCategory.PRACTICAL_SYSTEMS_CS, 4.0, 3, 0, 2, "A", 5.6, 5));
        aicte.add(new SubjectEntry("S503", "Computer Networks & Protocols", EctsCategory.PRACTICAL_SYSTEMS_CS, 4.0, 3, 0, 2, "A", 5.6, 5));
        aicte.add(new SubjectEntry("S504", "Software Engineering & Agile Methodologies", EctsCategory.SOFTWARE_ENGINEERING, 3.0, 3, 0, 0, "A+", 4.2, 5));
        aicte.add(new SubjectEntry("S601", "Compiler Design & Code Optimization", EctsCategory.SOFTWARE_ENGINEERING, 4.0, 3, 0, 2, "B+", 5.6, 6));
        aicte.add(new SubjectEntry("S602", "Artificial Intelligence & Heuristic Search", EctsCategory.ELECTIVES_OTHER, 3.0, 3, 0, 0, "A", 4.2, 6));
        aicte.add(new SubjectEntry("S603", "Complexity Theory & Advanced Automata", EctsCategory.THEORETICAL_CS, 4.0, 3, 1, 0, "A", 5.6, 6));
        aicte.add(new SubjectEntry("S701", "Machine Learning & Deep Neural Networks", EctsCategory.ELECTIVES_OTHER, 4.0, 3, 0, 2, "A+", 5.6, 7));
        aicte.add(new SubjectEntry("S702", "Distributed Systems & Cloud Infrastructure", EctsCategory.PRACTICAL_SYSTEMS_CS, 4.0, 3, 0, 2, "A", 5.6, 7));
        aicte.add(new SubjectEntry("S703", "Formal Verification & Program Semantics", EctsCategory.THEORETICAL_CS, 4.0, 3, 1, 0, "B+", 5.6, 7));
        aicte.add(new SubjectEntry("S801", "Final Bachelor Capstone Thesis / Project", EctsCategory.ELECTIVES_OTHER, 8.0, 0, 0, 16, "A+", 11.2, 8));

        sampleCurricula.add(new CurriculumSample(
            "AICTE_BTECH_CSE_MODEL",
            "AICTE Model Curriculum — B.Tech Computer Science (Full 4-Year)",
            "AICTE / Top IITs / NITs",
            "Standard recommended curriculum with balanced distribution across Theoretical CS, Mathematics, and Systems.",
            86.0,
            aicte
        ));

        // 2. Typical Tier-2/3 Indian Syllabus (Has Theoretical CS Deficit)
        List<SubjectEntry> tier2 = new ArrayList<>();
        tier2.add(new SubjectEntry("T101", "Engineering Mathematics I", EctsCategory.MATHEMATICS, 4.0, 3, 1, 0, "A", 5.6, 1));
        tier2.add(new SubjectEntry("T102", "Computer Programming (C Language)", EctsCategory.SOFTWARE_ENGINEERING, 4.0, 3, 0, 2, "A", 5.6, 1));
        tier2.add(new SubjectEntry("T201", "Engineering Mathematics II", EctsCategory.MATHEMATICS, 4.0, 3, 1, 0, "B+", 5.6, 2));
        tier2.add(new SubjectEntry("T202", "Basic Electronics Engineering", EctsCategory.PRACTICAL_SYSTEMS_CS, 3.0, 3, 0, 0, "B", 4.2, 2));
        tier2.add(new SubjectEntry("T301", "Discrete Structures", EctsCategory.MATHEMATICS, 4.0, 3, 1, 0, "B+", 5.6, 3));
        tier2.add(new SubjectEntry("T302", "Data Structures with C++", EctsCategory.THEORETICAL_CS, 4.0, 3, 0, 2, "A", 5.6, 3));
        tier2.add(new SubjectEntry("T303", "Computer Organization & Architecture", EctsCategory.PRACTICAL_SYSTEMS_CS, 4.0, 3, 0, 0, "B+", 5.6, 3));
        tier2.add(new SubjectEntry("T401", "Operating Systems", EctsCategory.PRACTICAL_SYSTEMS_CS, 4.0, 3, 0, 2, "A", 5.6, 4));
        tier2.add(new SubjectEntry("T402", "Software Engineering", EctsCategory.SOFTWARE_ENGINEERING, 3.0, 3, 0, 0, "A", 4.2, 4));
        tier2.add(new SubjectEntry("T403", "Database Management Systems", EctsCategory.PRACTICAL_SYSTEMS_CS, 4.0, 3, 0, 2, "A", 5.6, 4));
        tier2.add(new SubjectEntry("T501", "Theory of Automata & Formal Languages", EctsCategory.THEORETICAL_CS, 4.0, 3, 1, 0, "B+", 5.6, 5));
        tier2.add(new SubjectEntry("T502", "Design & Analysis of Algorithms", EctsCategory.THEORETICAL_CS, 4.0, 3, 1, 0, "B+", 5.6, 5));
        tier2.add(new SubjectEntry("T503", "Computer Networks", EctsCategory.PRACTICAL_SYSTEMS_CS, 4.0, 3, 0, 2, "A", 5.6, 5));
        tier2.add(new SubjectEntry("T601", "Compiler Design", EctsCategory.SOFTWARE_ENGINEERING, 4.0, 3, 0, 2, "B", 5.6, 6));
        tier2.add(new SubjectEntry("T602", "Web Technology & Full Stack", EctsCategory.SOFTWARE_ENGINEERING, 4.0, 3, 0, 2, "A+", 5.6, 6));
        tier2.add(new SubjectEntry("T603", "Information Security & Cyber Law", EctsCategory.ELECTIVES_OTHER, 3.0, 3, 0, 0, "A", 4.2, 6));
        tier2.add(new SubjectEntry("T701", "Cloud Computing & DevOps", EctsCategory.ELECTIVES_OTHER, 3.0, 3, 0, 0, "A", 4.2, 7));
        tier2.add(new SubjectEntry("T702", "Artificial Intelligence", EctsCategory.ELECTIVES_OTHER, 3.0, 3, 0, 0, "A", 4.2, 7));
        tier2.add(new SubjectEntry("T801", "Major Project / Industry Internship", EctsCategory.ELECTIVES_OTHER, 6.0, 0, 0, 12, "A+", 8.4, 8));

        sampleCurricula.add(new CurriculumSample(
            "TIER_2_3_INDIAN_CSE",
            "Indian State Technical University / Affiliated College (Common Theoretical CS Gap)",
            "AKTU / VTU / Anna Univ / State Tech Universities",
            "Common curriculum profile that frequently receives TUM/RWTH rejections due to 16.8 ECTS in Theoretical CS (missing 11+ ECTS).",
            69.0,
            tier2
        ));
    }

    private void initializeBridgeCatalog() {
        // Theoretical CS Remediation
        bridgeCatalog.add(new BridgeCourseRecommendation(
            "BR-01",
            EctsCategory.THEORETICAL_CS,
            "Automata, Computability, and Complexity (MIT 6.045J)",
            "MIT OpenCourseWare / edX",
            6.0,
            "12 Weeks (Self-Paced)",
            "https://ocw.mit.edu/courses/6-045j-automata-computability-and-complexity-spring-2011/",
            "Directly recognized by TUM and RWTH Aachen for Theoretical CS prerequisite fulfillment.",
            "Turing machines, Church-Turing thesis, decidability, halting problem, NP-completeness, space complexity."
        ));

        bridgeCatalog.add(new BridgeCourseRecommendation(
            "BR-02",
            EctsCategory.THEORETICAL_CS,
            "Theory of Computation by IIT Kanpur (Prof. Raghunath Tewari)",
            "NPTEL / Swayam (Govt. of India)",
            5.0,
            "8 Weeks (Proctored Exam Certificate)",
            "https://nptel.ac.in/courses/106104148",
            "NPTEL certificate with proctored exam is widely accepted by German universities and Uni-Assist.",
            "DFA/NFA, pumping lemma, context-free grammars, pushdown automata, Turing machines, undecidability."
        ));

        bridgeCatalog.add(new BridgeCourseRecommendation(
            "BR-03",
            EctsCategory.THEORETICAL_CS,
            "TUM Vorkurs: Foundations of Theoretical Informatics",
            "TUM Open edX / Virtual University of Bavaria (vhb)",
            6.0,
            "10 Weeks (Online)",
            "https://www.in.tum.de/en/in/current-students/master-programs/",
            "Official preparatory module recognized by TU Munich faculty for bridging bachelor deficits.",
            "Discrete structures, formal logic, propositional & predicate calculus, asymptotic notation, complexity classes."
        ));

        // Mathematics Remediation
        bridgeCatalog.add(new BridgeCourseRecommendation(
            "BR-04",
            EctsCategory.MATHEMATICS,
            "Discrete Mathematics by IIT Ropar (Prof. Sudarshan Iyengar)",
            "NPTEL / Swayam",
            6.0,
            "12 Weeks (Proctored Exam)",
            "https://nptel.ac.in/courses/106106183",
            "Mandatory if your bachelor program only had general engineering mathematics without separate Discrete Math.",
            "Mathematical reasoning, propositional logic, sets, relations, functions, graph theory, combinatorics."
        ));

        bridgeCatalog.add(new BridgeCourseRecommendation(
            "BR-05",
            EctsCategory.MATHEMATICS,
            "Linear Algebra (MIT 18.06 by Prof. Gilbert Strang)",
            "MIT OpenCourseWare",
            6.0,
            "12 Weeks (Self-Paced)",
            "https://ocw.mit.edu/courses/18-06-linear-algebra-spring-2010/",
            "Universal standard for German university mathematics equivalence checks.",
            "Vector spaces, eigenvalues, eigenvectors, singular value decomposition (SVD), positive definite matrices."
        ));

        bridgeCatalog.add(new BridgeCourseRecommendation(
            "BR-06",
            EctsCategory.MATHEMATICS,
            "Online-Mathematik-Brückenkurs (OMB+)",
            "RWTH Aachen & German Universities Consortium",
            5.0,
            "Self-Paced",
            "https://www.ombplus.de/",
            "Created specifically by German university mathematics departments for international Master's applicants.",
            "Differential and integral calculus, vector geometry, linear algebra, complex numbers."
        ));
    }
}
