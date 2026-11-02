package com.admissions.engine.repository;

import com.admissions.engine.model.ApplicantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link ApplicantProfile} entities.
 *
 * <p>Provides standard CRUD operations inherited from {@link JpaRepository}
 * plus custom derived queries for email lookup and filtered list retrieval.</p>
 */
@Repository
public interface ApplicantRepository extends JpaRepository<ApplicantProfile, Integer> {

    /**
     * Finds an applicant profile by their unique email address.
     *
     * @param email the applicant's email address
     * @return an {@link Optional} containing the profile if found, or empty if not
     */
    Optional<ApplicantProfile> findByEmail(String email);

    /**
     * Finds all applicants in a given specialization who graduated at or after the specified year.
     *
     * <p>Useful for filtering recent graduates by major when batch-scoring against programs.</p>
     *
     * @param specialization the undergraduate specialization to filter on (e.g. "Computer Science")
     * @param year           the minimum graduation year (inclusive)
     * @return list of matching applicant profiles; may be empty, never {@code null}
     */
    List<ApplicantProfile> findBySpecializationAndGraduationYearGreaterThanEqual(
            String specialization, int year);
}
