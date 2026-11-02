package com.admissions.engine.calculator;

import com.admissions.engine.model.ApplicantProfile;
import com.admissions.engine.model.DeficitReport;
import com.admissions.engine.model.UniversityProgram;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring component that implements {@link DeficitReportable} to produce a detailed
 * gap analysis between an applicant's credentials and a program's admission thresholds.
 *
 * <p>Each {@code analyze*()} method checks one dimension and, when a deficit is found,
 * records both a human-readable delta description and an actionable recommendation.</p>
 */
@Component
public class DeficitAnalyzer implements DeficitReportable {

    /**
     * Constructs a {@link DeficitReport} by checking all relevant credential dimensions
     * in sequence (GPA, GRE, JLPT, IELTS, TOEFL, ECTS, Thesis).
     *
     * {@inheritDoc}
     */
    @Override
    public DeficitReport analyzeDeficit(ApplicantProfile profile, UniversityProgram program) {
        Map<String, String> deltas = new LinkedHashMap<>();
        List<String> recommendations = new ArrayList<>();

        analyzeGpa(profile, program, deltas, recommendations);
        analyzeGreQuant(profile, program, deltas, recommendations);
        analyzeGreVerbal(profile, program, deltas, recommendations);
        analyzeGreAw(profile, program, deltas, recommendations);
        analyzeJlpt(profile, program, deltas, recommendations);
        analyzeIelts(profile, program, deltas, recommendations);
        analyzeToefl(profile, program, deltas, recommendations);
        analyzeEcts(profile, program, deltas, recommendations);
        analyzeThesis(profile, program, deltas, recommendations);

        boolean meetsAll = deltas.isEmpty();
        return new DeficitReport(deltas, recommendations, meetsAll);
    }

    // ── Private Analyze Methods ──────────────────────────────────────────────

    /**
     * Checks if the applicant's normalized GPA meets the program's GPA threshold.
     *
     * @param profile         applicant profile
     * @param program         target program
     * @param deltas          map to record any GPA deficit
     * @param recommendations list to append actionable advice
     */
    private void analyzeGpa(ApplicantProfile profile,
                             UniversityProgram program,
                             Map<String, String> deltas,
                             List<String> recommendations) {
        if (program.getThresholdGpa() == null) return;

        // Convert 10-pt CGPA to US 4.0 for comparison
        double us4pt = (profile.getCgpa10pt() / profile.getCgpaScale()) * 4.0;
        double threshold = program.getThresholdGpa();

        if (us4pt < threshold) {
            double gap = threshold - us4pt;
            deltas.put("gpa", String.format(
                    "Deficit of %.2f GPA points (%.2f required on 4.0 scale, %.2f provided)",
                    gap, threshold, us4pt));
            recommendations.add(String.format(
                    "Improve cumulative GPA to at least %.2f/4.0. "
                  + "Consider additional coursework, grade replacements, or a post-baccalaureate program.", threshold));
        }
    }

    /**
     * Checks if the applicant's GRE Quantitative score meets the program's threshold.
     */
    private void analyzeGreQuant(ApplicantProfile profile,
                                  UniversityProgram program,
                                  Map<String, String> deltas,
                                  List<String> recommendations) {
        if (program.getThresholdGreQ() == null || profile.getGreQuant() == null) return;

        int required = program.getThresholdGreQ();
        int provided = profile.getGreQuant();

        if (provided < required) {
            int gap = required - provided;
            deltas.put("greQuant", String.format(
                    "Deficit of %d points (GRE Quant ≥ %d required, %d provided)",
                    gap, required, provided));
            recommendations.add(String.format(
                    "Retake GRE Quantitative to achieve a minimum score of %d. "
                  + "Focus on Algebra, Geometry, and Data Interpretation.", required));
        }
    }

    /**
     * Checks if the applicant's GRE Verbal score meets the program's threshold.
     */
    private void analyzeGreVerbal(ApplicantProfile profile,
                                   UniversityProgram program,
                                   Map<String, String> deltas,
                                   List<String> recommendations) {
        if (program.getThresholdGreV() == null || profile.getGreVerbal() == null) return;

        int required = program.getThresholdGreV();
        int provided = profile.getGreVerbal();

        if (provided < required) {
            int gap = required - provided;
            deltas.put("greVerbal", String.format(
                    "Deficit of %d points (GRE Verbal ≥ %d required, %d provided)",
                    gap, required, provided));
            recommendations.add(String.format(
                    "Retake GRE Verbal to achieve a minimum score of %d. "
                  + "Practice Reading Comprehension and Vocabulary.", required));
        }
    }

    /**
     * Checks if the applicant's GRE Analytical Writing score meets the program's threshold.
     */
    private void analyzeGreAw(ApplicantProfile profile,
                               UniversityProgram program,
                               Map<String, String> deltas,
                               List<String> recommendations) {
        if (program.getThresholdGreAw() == null || profile.getGreAw() == null) return;

        double required = program.getThresholdGreAw();
        double provided = profile.getGreAw();

        if (provided < required) {
            double gap = required - provided;
            deltas.put("greAw", String.format(
                    "Deficit of %.1f points (GRE AW ≥ %.1f required, %.1f provided)",
                    gap, required, provided));
            recommendations.add(String.format(
                    "Retake GRE Analytical Writing to achieve a minimum score of %.1f. "
                  + "Practice Analyze-an-Issue and Analyze-an-Argument essays.", required));
        }
    }

    /**
     * Checks if the applicant's JLPT level meets the program's minimum JLPT requirement.
     *
     * <p>JLPT levels are inversely ordered: N1 > N2 > N3 > N4 > N5. A numeric comparison
     * is performed on the digit suffix (lower digit = higher proficiency).</p>
     */
    private void analyzeJlpt(ApplicantProfile profile,
                              UniversityProgram program,
                              Map<String, String> deltas,
                              List<String> recommendations) {
        if (program.getThresholdJlpt() == null || profile.getJlptLevel() == null) return;

        int required = jlptToInt(program.getThresholdJlpt());
        int provided = jlptToInt(profile.getJlptLevel());

        // Lower number = higher proficiency; deficit when provided > required
        if (provided > required) {
            deltas.put("jlptLevel", String.format(
                    "JLPT %s required, applicant holds %s (insufficient proficiency)",
                    program.getThresholdJlpt(), profile.getJlptLevel()));
            recommendations.add(String.format(
                    "Obtain JLPT %s or higher. Enroll in an intensive Japanese language course "
                  + "and schedule the next available JLPT sitting.", program.getThresholdJlpt()));
        }
    }

    /**
     * Checks if the applicant's IELTS score meets the program's threshold.
     */
    private void analyzeIelts(ApplicantProfile profile,
                               UniversityProgram program,
                               Map<String, String> deltas,
                               List<String> recommendations) {
        if (program.getThresholdIelts() == null || profile.getIeltsScore() == null) return;

        double required = program.getThresholdIelts();
        double provided = profile.getIeltsScore();

        if (provided < required) {
            double gap = required - provided;
            deltas.put("ieltsScore", String.format(
                    "Deficit of %.1f bands (IELTS ≥ %.1f required, %.1f provided)",
                    gap, required, provided));
            recommendations.add(String.format(
                    "Retake IELTS Academic to achieve at least %.1f overall. "
                  + "Focus on Writing and Speaking sub-bands.", required));
        }
    }

    /**
     * Checks if the applicant's TOEFL iBT score meets the program's threshold.
     */
    private void analyzeToefl(ApplicantProfile profile,
                               UniversityProgram program,
                               Map<String, String> deltas,
                               List<String> recommendations) {
        if (program.getThresholdToefl() == null || profile.getToeflScore() == null) return;

        int required = program.getThresholdToefl();
        int provided = profile.getToeflScore();

        if (provided < required) {
            int gap = required - provided;
            deltas.put("toeflScore", String.format(
                    "Deficit of %d points (TOEFL ≥ %d required, %d provided)",
                    gap, required, provided));
            recommendations.add(String.format(
                    "Retake TOEFL iBT to score at least %d. "
                  + "Practice all four sections, especially Writing and Speaking.", required));
        }
    }

    /**
     * Checks if the applicant's ECTS CS credits meet the program's minimum requirement.
     */
    private void analyzeEcts(ApplicantProfile profile,
                              UniversityProgram program,
                              Map<String, String> deltas,
                              List<String> recommendations) {
        if (program.getThresholdEctsCs() == null || profile.getEctsCreditsCs() == null) return;

        int required = program.getThresholdEctsCs();
        int provided = profile.getEctsCreditsCs();

        if (provided < required) {
            int gap = required - provided;
            deltas.put("ectsCreditsCs", String.format(
                    "Deficit of %d ECTS credits in CS (%d required, %d provided)",
                    gap, required, provided));
            recommendations.add(String.format(
                    "Complete additional CS coursework to earn at least %d more ECTS credits. "
                  + "Consider online courses recognized by the target institution.", gap));
        }
    }

    /**
     * Checks whether the program requires a thesis and whether the applicant has one.
     */
    private void analyzeThesis(ApplicantProfile profile,
                                UniversityProgram program,
                                Map<String, String> deltas,
                                List<String> recommendations) {
        if (!Boolean.TRUE.equals(program.getThesisRequired())) return;
        if (!Boolean.TRUE.equals(profile.getThesisCompleted())) {
            deltas.put("thesisCompleted",
                    "Thesis required by program but not completed by applicant");
            recommendations.add(
                    "Complete an undergraduate thesis or independent research project. "
                  + "Alternatively, pursue a research internship that yields a written report.");
        }
    }

    // ── Utility ──────────────────────────────────────────────────────────────

    /**
     * Converts a JLPT level string (e.g. "N2") to its numeric suffix as an integer.
     *
     * @param level JLPT level string
     * @return integer level (1–5); returns 99 for unrecognized strings
     */
    private int jlptToInt(String level) {
        if (level == null || level.length() < 2) return 99;
        try {
            return Integer.parseInt(level.trim().substring(1));
        } catch (NumberFormatException e) {
            return 99;
        }
    }
}
