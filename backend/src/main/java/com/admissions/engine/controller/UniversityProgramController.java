package com.admissions.engine.controller;

import com.admissions.engine.model.UniversityProgram;
import com.admissions.engine.repository.UniversityProgramRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/programs")
@CrossOrigin(originPatterns = "*")
public class UniversityProgramController {

    private final UniversityProgramRepository programRepository;

    public UniversityProgramController(UniversityProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    @GetMapping
    public ResponseEntity<List<UniversityProgram>> getAllPrograms(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String specialization) {

        List<UniversityProgram> programs;
        if (region != null && !region.isBlank() && !"ALL".equalsIgnoreCase(region.trim())) {
            programs = programRepository.findByRegion(region.trim().toUpperCase());
        } else {
            programs = programRepository.findAll();
        }

        if (specialization != null && !specialization.isBlank()) {
            programs = programs.stream()
                    .filter(p -> p.getSpecialization() != null &&
                            p.getSpecialization().toLowerCase().contains(specialization.toLowerCase()))
                    .toList();
        }

        return ResponseEntity.ok(programs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UniversityProgram> getProgramById(@PathVariable int id) {
        return programRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
