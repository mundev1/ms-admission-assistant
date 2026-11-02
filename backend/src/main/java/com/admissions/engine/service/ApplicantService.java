package com.admissions.engine.service;

import com.admissions.engine.model.ApplicantProfile;
import com.admissions.engine.repository.ApplicantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Application service providing CRUD operations for {@link ApplicantProfile} entities.
 *
 * <p>Acts as the primary entry point for applicant data management, delegating
 * persistence operations to {@link ApplicantRepository}.</p>
 */
@Service
public class ApplicantService {

    private final ApplicantRepository applicantRepository;

    /**
     * Constructs the service with its required repository dependency.
     *
     * @param applicantRepository the JPA repository for applicant profiles
     */
    public ApplicantService(ApplicantRepository applicantRepository) {
        this.applicantRepository = applicantRepository;
    }

    /**
     * Persists a new applicant profile or updates an existing one (by primary key or email).
     *
     * @param profile the profile to save; must not be {@code null}
     * @return the saved {@link ApplicantProfile} with any generated fields populated
     */
    public ApplicantProfile save(ApplicantProfile profile) {
        if (profile.getEmail() != null && !profile.getEmail().isBlank()) {
            Optional<ApplicantProfile> existing = applicantRepository.findByEmail(profile.getEmail());
            if (existing.isPresent()) {
                profile.setApplicantId(existing.get().getApplicantId());
            }
        }
        return applicantRepository.save(profile);
    }

    /**
     * Finds an applicant by their numeric primary key.
     *
     * @param id the applicant ID
     * @return an {@link Optional} containing the profile, or empty if not found
     */
    public Optional<ApplicantProfile> findById(int id) {
        return applicantRepository.findById(id);
    }

    /**
     * Finds an applicant by their unique email address.
     *
     * @param email the applicant's email address
     * @return an {@link Optional} containing the profile, or empty if no match found
     */
    public Optional<ApplicantProfile> findByEmail(String email) {
        return applicantRepository.findByEmail(email);
    }

    /**
     * Returns all applicant profiles in the database, ordered by insertion by default.
     *
     * @return list of all {@link ApplicantProfile} records; may be empty, never {@code null}
     */
    public List<ApplicantProfile> findAll() {
        return applicantRepository.findAll();
    }

    /**
     * Deletes the applicant profile with the given ID.
     *
     * @param id the ID of the profile to delete
     * @throws NoSuchElementException if no profile exists with the given ID
     */
    public void deleteById(int id) {
        if (!applicantRepository.existsById(id)) {
            throw new NoSuchElementException("Applicant not found with id: " + id);
        }
        applicantRepository.deleteById(id);
    }
}
