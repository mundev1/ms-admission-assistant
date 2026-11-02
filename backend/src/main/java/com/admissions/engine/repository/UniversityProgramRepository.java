package com.admissions.engine.repository;

import com.admissions.engine.model.UniversityProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link UniversityProgram} entities.
 *
 * <p>Provides CRUD operations plus custom queries for region/country filtering
 * and threshold-based program discovery.</p>
 */
@Repository
public interface UniversityProgramRepository extends JpaRepository<UniversityProgram, Integer> {

    /**
     * Retrieves all programs belonging to the specified geographic region.
     *
     * @param region the region key (e.g. "US_CANADA", "EUROPE", "JAPAN")
     * @return list of programs in that region; may be empty, never {@code null}
     */
    List<UniversityProgram> findByRegion(String region);

    /**
     * Retrieves all programs in a given country that match the specified specialization.
     *
     * @param country        the country name (e.g. "Germany", "USA")
     * @param specialization the program specialization (e.g. "Computer Science")
     * @return list of matching programs; may be empty, never {@code null}
     */
    List<UniversityProgram> findByCountryAndSpecialization(String country, String specialization);

    /**
     * Finds programs within a specific region whose minimum GPA threshold is
     * at or below the applicant's normalized GPA, making them viable targets.
     *
     * <p>Programs with a {@code NULL} threshold are excluded from results.</p>
     *
     * @param region the region key to filter on
     * @param gpa    the applicant's normalized GPA (on the 4.0 scale)
     * @return list of programs whose threshold is achievable at the given GPA
     */
    @Query("SELECT p FROM UniversityProgram p "
         + "WHERE p.region = :region "
         + "AND p.thresholdGpa IS NOT NULL "
         + "AND p.thresholdGpa <= :gpa "
         + "ORDER BY p.thresholdGpa DESC")
    List<UniversityProgram> findByRegionAndGpaThresholdAtMost(
            @Param("region") String region,
            @Param("gpa")    double gpa);
}
