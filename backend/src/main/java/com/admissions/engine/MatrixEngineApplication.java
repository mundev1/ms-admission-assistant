package com.admissions.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║   Cross-Border MS Admissions Matrix Engine                   ║
 * ║   Spring Boot Application Entry Point                        ║
 * ║   Version: 1.0.0                                             ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * <p>This application provides a REST API for scoring applicant
 * profiles against university programs across US/Canada, Europe,
 * and Japan using region-specific weighting matrices.</p>
 */
@SpringBootApplication
public class MatrixEngineApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments passed to the Spring context
     */
    public static void main(String[] args) {
        SpringApplication.run(MatrixEngineApplication.class, args);
    }
}
