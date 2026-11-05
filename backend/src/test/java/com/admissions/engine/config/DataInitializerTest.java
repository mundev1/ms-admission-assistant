package com.admissions.engine.config;

import com.admissions.engine.repository.UniversityProgramRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DataInitializerTest {

    @Autowired
    private UniversityProgramRepository programRepository;

    @Test
    void testSeededProgramsCount() {
        long count = programRepository.count();
        assertTrue(count >= 50, "Expected at least 50 seeded university programs, but found: " + count);
    }
}
