package com.admissions.engine.calculator;

import com.admissions.engine.model.ApplicantProfile;
import com.admissions.engine.model.UniversityProgram;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * FinancialVisaEvaluator — Evaluates financial feasibility, scholarship eligibility,
 * and post-study work visa advantages alongside academic compatibility.
 */
@Component
public class FinancialVisaEvaluator {

    public record FinancialVisaEvaluation(
            double financialScore,
            double visaScore,
            double holisticScore,
            String holisticTier,
            boolean scholarshipEligible,
            List<String> matchedScholarships,
            String financialSummary,
            String visaSummary
    ) {}

    /**
     * Evaluates financial feasibility and post-study work visa outlook for an applicant
     * against a university program, blending them with the academic compatibility score.
     *
     * @param profile       applicant academic and financial profile
     * @param program       target university program with cost and visa policies
     * @param academicScore computed academic compatibility score (0-100)
     * @return full financial and visa evaluation result
     */
    public FinancialVisaEvaluation evaluate(ApplicantProfile profile, UniversityProgram program, double academicScore) {
        // ── 1. Financial Feasibility Evaluation ──────────────────────────────
        double tuition = program.getTuitionAnnualUsd() != null ? program.getTuitionAnnualUsd() : 25000.0;
        double livingCost = program.getLivingCostAnnualUsd() != null ? program.getLivingCostAnnualUsd() : 15000.0;
        double netAnnualCost = tuition + livingCost;

        double budget = profile.getAnnualBudgetUsd() != null ? profile.getAnnualBudgetUsd() : 30000.0;
        double savings = profile.getLiquidSavingsUsd() != null ? profile.getLiquidSavingsUsd() : 20000.0;
        double blockedReq = program.getBlockedAccountRequirementUsd() != null
                ? program.getBlockedAccountRequirementUsd() : 12000.0;

        // 1a. Self-funding coverage factor [0.1, 1.0]
        double fFund = (budget >= netAnnualCost)
                ? 1.0
                : Math.max(0.15, budget / netAnnualCost);

        // 1b. Liquid funds / Blocked account factor [0.2, 1.0]
        double fLiquid = (blockedReq <= 0 || savings >= blockedReq)
                ? 1.0
                : Math.max(0.20, savings / blockedReq);

        // 1c. Scholarship Eligibility Matching
        List<String> matchedScholarships = new ArrayList<>();
        double scholarshipCoverage = evaluateScholarships(profile, program, matchedScholarships);

        // Compute Financial Score (0 - 100)
        double rawFin = (0.50 * fFund + 0.25 * fLiquid + 0.25 * scholarshipCoverage) * 100.0;

        // Boost financial score if student qualified for full-ride awards (e.g. MEXT, DAAD)
        if (!matchedScholarships.isEmpty() && scholarshipCoverage >= 0.85) {
            rawFin = Math.max(rawFin, 90.0);
        }

        double financialScore = Math.round(Math.min(100.0, Math.max(10.0, rawFin)) * 10.0) / 10.0;

        // Financial summary note
        String financialSummary;
        if (budget >= netAnnualCost) {
            financialSummary = String.format("Self-funded budget ($%,.0f) fully covers estimated annual attendance cost ($%,.0f).",
                    budget, netAnnualCost);
        } else if (!matchedScholarships.isEmpty()) {
            financialSummary = String.format("Annual cost is $%,.0f (Budget: $%,.0f). High scholarship eligibility (%s) bridges the funding deficit.",
                    netAnnualCost, budget, matchedScholarships.get(0).split("—")[0].trim());
        } else {
            double gap = netAnnualCost - budget;
            financialSummary = String.format("Annual cost is $%,.0f (Budget: $%,.0f). Funding gap of $%,.0f/yr requires educational loan or assistantship.",
                    netAnnualCost, budget, gap);
        }

        // ── 2. Post-Study Work Visa Advantage Evaluation ─────────────────────
        String priority = profile.getPostStudyWorkPriority() != null
                ? profile.getPostStudyWorkPriority().toUpperCase().trim() : "HIGH";

        double baseVisaScore = resolveBaseVisaScore(program);
        double visaScore;

        if ("LOW".equals(priority) || "NONE".equals(priority)) {
            visaScore = 100.0; // Student does not prioritize post-study work; don't penalize
        } else if ("MODERATE".equals(priority)) {
            visaScore = Math.round((baseVisaScore * 0.7 + 100.0 * 0.3) * 10.0) / 10.0;
        } else {
            visaScore = baseVisaScore;
        }

        String visaSummary = buildVisaSummary(program);

        // ── 3. Holistic Index: 50% Academic + 30% Financial + 20% Visa ───────
        double rawHolistic = (0.50 * academicScore) + (0.30 * financialScore) + (0.20 * visaScore);
        double holisticScore = Math.round(rawHolistic * 10.0) / 10.0;
        String holisticTier = MatrixCalculator.classifyTier(holisticScore);

        return new FinancialVisaEvaluation(
                financialScore,
                visaScore,
                holisticScore,
                holisticTier,
                !matchedScholarships.isEmpty(),
                matchedScholarships,
                financialSummary,
                visaSummary
        );
    }

    /**
     * Checks eligibility for international government and institutional scholarships.
     * Returns a funding coverage factor between [0.1, 1.0].
     */
    private double evaluateScholarships(ApplicantProfile p, UniversityProgram prog, List<String> matches) {
        double maxCoverage = 0.20; // baseline incidental aid
        String region = prog.getRegion() != null ? prog.getRegion().toUpperCase() : "";
        String country = prog.getCountry() != null ? prog.getCountry().toLowerCase() : "";

        double cgpa = p.getCgpa10pt();
        int greQ = p.getGreQuant() != null ? p.getGreQuant() : 130;
        int toefl = p.getToeflScore() != null ? p.getToeflScore() : 0;
        double ielts = p.getIeltsScore() != null ? p.getIeltsScore() : 0.0;
        boolean thesis = Boolean.TRUE.equals(p.getThesisCompleted());
        double research = p.getResearchScore() != null ? p.getResearchScore() : 0.0;
        String jlpt = p.getJlptLevel() != null ? p.getJlptLevel().toUpperCase() : "";

        // 1. Japanese MEXT Scholarship & MIRAI-Setu
        if ("JAPAN".equals(region) || country.contains("japan")) {
            if (cgpa >= 8.0 && (research >= 75.0 || jlpt.equals("N1") || jlpt.equals("N2"))) {
                matches.add("MEXT Japanese Government Scholarship — Full tuition waiver + ¥144,000/mo living stipend");
                maxCoverage = Math.max(maxCoverage, 0.95);
            } else if (cgpa >= 7.5) {
                matches.add("JASSO Honors Scholarship — ¥48,000/mo study stipend for international students");
                maxCoverage = Math.max(maxCoverage, 0.60);
            }
        }

        // 2. German DAAD Scholarship
        if (country.contains("germany") || country.contains("deutschland")) {
            if (cgpa >= 8.5 && (thesis || p.getInternshipMonths() >= 6)) {
                matches.add("DAAD Master Studies Scholarship — €934/mo stipend + travel + health coverage");
                maxCoverage = Math.max(maxCoverage, 0.90);
            }
        }

        // 3. US Fulbright Foreign Student Program
        if ("US_CANADA".equals(region) && country.contains("usa")) {
            if (cgpa >= 8.8 && greQ >= 165 && (toefl >= 100 || ielts >= 7.5)) {
                matches.add("Fulbright Foreign Student Program — Comprehensive tuition grant + monthly stipend");
                maxCoverage = Math.max(maxCoverage, 0.85);
            }
        }

        // 4. Departmental Graduate Teaching / Research Assistantship (TA / RA)
        if (greQ >= 164 && cgpa >= 8.2 && (thesis || research >= 70.0)) {
            matches.add("Graduate Teaching/Research Assistantship (TA/RA) — Tuition remission + departmental salary");
            maxCoverage = Math.max(maxCoverage, 0.80);
        }

        return maxCoverage;
    }

    /**
     * Resolves the baseline post-study work visa score based on destination country policies.
     */
    private double resolveBaseVisaScore(UniversityProgram program) {
        String country = program.getCountry() != null ? program.getCountry().toLowerCase() : "";
        String region = program.getRegion() != null ? program.getRegion().toUpperCase() : "";

        if (country.contains("canada")) {
            // Canada PGWP: up to 3 years open work permit without employer sponsorship, strong PR pathway
            return 95.0;
        } else if (country.contains("germany")) {
            // Germany: 18-month job seeker visa with unrestricted work authorization + 21-27 month EU Blue Card to PR
            return 92.0;
        } else if (country.contains("usa") || country.contains("united states")) {
            // US: 36-month STEM OPT (12 mos + 24 mos extension), tied to E-Verify employer, H-1B cap subject
            return 85.0;
        } else if (country.contains("japan") || "JAPAN".equals(region)) {
            // Japan: J-Find 2-year job seeker visa / Highly Skilled Professional (HSP) points system (PR in 1-3 years)
            return 82.0;
        } else if (country.contains("switzerland")) {
            return 80.0;
        } else if (country.contains("sweden") || country.contains("netherlands") || country.contains("uk")) {
            return 84.0;
        }
        return 80.0;
    }

    private String buildVisaSummary(UniversityProgram program) {
        String country = program.getCountry() != null ? program.getCountry().toLowerCase() : "";
        String progName = program.getVisaProgramName();
        if (progName != null && !progName.isBlank()) {
            return progName;
        }

        if (country.contains("canada")) {
            return "Canada PGWP: Up to 36 months open work permit with direct Express Entry/PNP permanent residency pathways.";
        } else if (country.contains("germany")) {
            return "Germany: 18-month Job Seeker Visa with unrestricted work rights; eligible for EU Blue Card and PR in 21–27 months.";
        } else if (country.contains("usa") || country.contains("united states")) {
            return "USA: 36-Month STEM OPT extension (12+24 mos) with E-Verify employer; transitions to H-1B lottery.";
        } else if (country.contains("japan")) {
            return "Japan: J-Find / Designated Activities (up to 24 mos) for job hunting; fast-track PR (1–3 yrs) via Highly Skilled Professional points.";
        } else {
            return "Post-study work authorization available under destination country immigration regulations.";
        }
    }
}
