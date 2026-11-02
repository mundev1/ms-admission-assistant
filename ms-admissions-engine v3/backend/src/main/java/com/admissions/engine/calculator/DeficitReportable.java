package com.admissions.engine.calculator;

import com.admissions.engine.model.ApplicantProfile;
import com.admissions.engine.model.DeficitReport;
import com.admissions.engine.model.UniversityProgram;

/**
 * Strategy interface for analyzing the gap between an applicant's credentials
 * and a program's admission thresholds.
 *
 * <p>Implementations inspect each relevant field (GPA, GRE, language, ECTS, thesis)
 * and populate a {@link DeficitReport} with per-field deltas and recommendations.</p>
 */
public interface DeficitReportable {

    /**
     * Produces a detailed deficit analysis for the given applicant–program pair.
     *
     * @param profile the applicant's academic profile; must not be {@code null}
     * @param program the target university program; must not be {@code null}
     * @return a fully populated {@link DeficitReport}
     */
    DeficitReport analyzeDeficit(ApplicantProfile profile, UniversityProgram program);
}
