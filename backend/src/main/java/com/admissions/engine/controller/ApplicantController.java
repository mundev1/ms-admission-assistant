package com.admissions.engine.controller;

import com.admissions.engine.model.ApplicantProfile;
import com.admissions.engine.service.ApplicantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * REST controller exposing CRUD endpoints for {@link ApplicantProfile} resources.
 *
 * <p>Base path: {@code /api/applicants}</p>
 */
@RestController
@RequestMapping("/api/applicants")
public class ApplicantController {

    private final ApplicantService applicantService;

    /**
     * Constructs the controller with the required service dependency.
     *
     * @param applicantService the applicant CRUD service
     */
    public ApplicantController(ApplicantService applicantService) {
        this.applicantService = applicantService;
    }

    // ── POST /api/applicants ─────────────────────────────────────────────────

    /**
     * Creates and persists a new applicant profile.
     *
     * @param profile the validated applicant profile from the request body
     * @return {@code 201 Created} with the persisted profile (including generated ID)
     */
    @PostMapping
    public ResponseEntity<ApplicantProfile> create(@Valid @RequestBody ApplicantProfile profile) {
        ApplicantProfile saved = applicantService.save(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ── GET /api/applicants/{id} ─────────────────────────────────────────────

    /**
     * Retrieves a single applicant profile by ID.
     *
     * @param id the applicant's primary key (path variable)
     * @return {@code 200 OK} with the profile, or {@code 404 Not Found}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApplicantProfile> findById(@PathVariable int id) {
        return applicantService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── GET /api/applicants[?email=...] ─────────────────────────────────────

    /**
     * Retrieves all applicant profiles, with an optional email filter.
     *
     * @param email optional query parameter; when provided, returns only the matching profile
     * @return {@code 200 OK} with a list of profiles (empty list if none found)
     */
    @GetMapping
    public ResponseEntity<List<ApplicantProfile>> findAll(
            @RequestParam(name = "email", required = false) String email) {

        if (email != null && !email.isBlank()) {
            return applicantService.findByEmail(email)
                    .map(profile -> ResponseEntity.ok(List.of(profile)))
                    .orElse(ResponseEntity.ok(List.of()));
        }
        return ResponseEntity.ok(applicantService.findAll());
    }

    // ── PUT /api/applicants/{id} ─────────────────────────────────────────────

    /**
     * Updates an existing applicant profile.
     *
     * <p>The path {@code id} is written onto the request body before saving, ensuring
     * the correct record is updated regardless of the body's own {@code applicantId}.</p>
     *
     * @param id      the ID of the profile to update
     * @param profile the updated profile data
     * @return {@code 200 OK} with the updated profile, or {@code 404 Not Found}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApplicantProfile> update(
            @PathVariable int id,
            @Valid @RequestBody ApplicantProfile profile) {

        return applicantService.findById(id)
                .map(existing -> {
                    profile.setApplicantId(id);
                    return ResponseEntity.ok(applicantService.save(profile));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── DELETE /api/applicants/{id} ──────────────────────────────────────────

    /**
     * Deletes the applicant profile with the specified ID.
     *
     * @param id the ID of the profile to delete
     * @return {@code 204 No Content} on success, or {@code 404 Not Found}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        try {
            applicantService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
