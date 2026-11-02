package com.admissions.engine.calculator;

import com.admissions.engine.model.ApplicantProfile;
import com.admissions.engine.model.ScoreResult;
import com.admissions.engine.model.UniversityProgram;

/**
 * Strategy interface for computing a compatibility score between an applicant
 * and a university program.
 *
 * <p>Implementations apply region-specific weighting matrices to produce a
 * {@link ScoreResult} with a score in [0, 100] and a tier classification.</p>
 */
public interface Scorable {

    /**
     * Calculates a weighted compatibility score for the given applicant–program pair.
     *
     * @param profile the applicant's academic profile; must not be {@code null}
     * @param program the target university program (must have weights attached); must not be {@code null}
     * @return a fully populated {@link ScoreResult}
     */
    ScoreResult calculateScore(ApplicantProfile profile, UniversityProgram program);
}
