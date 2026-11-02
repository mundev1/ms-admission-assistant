package com.admissions.engine.repository;

import com.admissions.engine.model.ScoringWeights;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link ScoringWeights} entities.
 *
 * <p>Supports lookup of the currently active weight configuration for a given
 * regional framework, ensuring the scoring engine always uses consistent weights.</p>
 */
@Repository
public interface ScoringWeightsRepository extends JpaRepository<ScoringWeights, Integer> {

    /**
     * Finds the active scoring weights for the specified framework.
     *
     * <p>At most one active weight row per framework should exist at any given time.
     * If multiple rows have {@code isActive = true} for the same framework, the first
     * result from the underlying query is returned.</p>
     *
     * @param frameworkId the framework identifier (FK to {@link com.admissions.engine.model.CountryFramework})
     * @return an {@link Optional} containing the active weights, or empty if none found
     */
    Optional<ScoringWeights> findByFrameworkIdAndIsActiveTrue(int frameworkId);
}
