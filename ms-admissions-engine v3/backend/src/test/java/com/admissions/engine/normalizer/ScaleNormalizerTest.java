package com.admissions.engine.normalizer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ScaleNormalizer Unit Tests")
class ScaleNormalizerTest {

    private ScaleNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = ScaleNormalizer.getInstance();
    }

    @Test
    @DisplayName("Singleton instance should not be null and should be consistent")
    void testSingleton() {
        assertNotNull(normalizer);
        assertSame(normalizer, ScaleNormalizer.getInstance());
    }

    @ParameterizedTest(name = "Indian CGPA {0} -> US 4.0 GPA expected {1}")
    @CsvSource({
        "10.0, 4.0",
        "8.75, 3.5",
        "7.5, 3.0",
        "5.0, 2.0",
        "0.0, 0.0"
    })
    void testNormalizeToUS4pt(double cgpa, double expected) {
        assertEquals(expected, normalizer.normalizeToUS4pt(cgpa), 0.01);
    }

    @Test
    @DisplayName("Invalid CGPA bounds should throw IllegalArgumentException")
    void testNormalizeToUS4ptInvalidBounds() {
        assertThrows(IllegalArgumentException.class, () -> normalizer.normalizeToUS4pt(-0.5));
        assertThrows(IllegalArgumentException.class, () -> normalizer.normalizeToUS4pt(10.5));
    }

    @ParameterizedTest(name = "Indian CGPA {0} -> Japanese 4.0 Scale expected approx {1}")
    @CsvSource({
        "9.0, 4.0",
        "8.0, 3.50",
        "7.0, 3.00",
        "5.0, 2.00",
        "2.0, 0.80"
    })
    void testNormalizeToJapaneseGpa4pt(double cgpa, double expected) {
        assertEquals(expected, normalizer.normalizeToJapaneseGpa(cgpa, 4.0), 0.05);
    }

    @Test
    @DisplayName("Japanese 3.0 scale scaling verification")
    void testNormalizeToJapaneseGpa3pt() {
        double gpa4 = normalizer.normalizeToJapaneseGpa(8.0, 4.0);
        double gpa3 = normalizer.normalizeToJapaneseGpa(8.0, 3.0);
        assertEquals(gpa4 * 0.75, gpa3, 0.02);
    }

    @ParameterizedTest(name = "Indian CGPA {0} -> ECTS Normalized Grade {1}")
    @CsvSource({
        "9.5, 1.0",
        "8.2, 0.8",
        "7.1, 0.6",
        "5.5, 0.4",
        "4.2, 0.0"
    })
    void testNormalizeToEctsGrade(double cgpa, double expected) {
        assertEquals(expected, normalizer.normalizeToEctsGrade(cgpa), 0.01);
    }

    @ParameterizedTest(name = "GRE raw score {0} -> normalized {1}")
    @CsvSource({
        "170, 1.0",
        "150, 0.5",
        "130, 0.0"
    })
    void testNormalizeGreScore(int raw, double expected) {
        assertEquals(expected, normalizer.normalizeGreScore(raw), 0.01);
    }

    @Test
    @DisplayName("GRE score outside 130-170 should throw IllegalArgumentException")
    void testNormalizeGreScoreInvalid() {
        assertThrows(IllegalArgumentException.class, () -> normalizer.normalizeGreScore(129));
        assertThrows(IllegalArgumentException.class, () -> normalizer.normalizeGreScore(171));
    }

    @ParameterizedTest(name = "JLPT Level {0} -> Score {1}")
    @CsvSource({
        "N1, 1.0",
        "N2, 0.8",
        "N3, 0.6",
        "N4, 0.4",
        "N5, 0.2"
    })
    void testNormalizeJlpt(String level, double expected) {
        assertEquals(expected, normalizer.normalizeJlpt(level), 0.01);
    }

    @Test
    @DisplayName("Empty or null JLPT should return 0.0")
    void testNormalizeJlptNullOrBlank() {
        assertEquals(0.0, normalizer.normalizeJlpt(null));
        assertEquals(0.0, normalizer.normalizeJlpt(""));
        assertEquals(0.0, normalizer.normalizeJlpt("   "));
    }

    @Test
    @DisplayName("ECTS credits should be capped at 1.0")
    void testNormalizeEctsCredits() {
        assertEquals(1.0, normalizer.normalizeEctsCredits(120, 90), 0.01);
        assertEquals(0.5, normalizer.normalizeEctsCredits(45, 90), 0.01);
        assertEquals(1.0, normalizer.normalizeEctsCredits(10, 0), 0.01);
    }
}
