-- ============================================================
-- Cross-Border MS Admissions Matrix Engine
-- Database Schema v1.0
-- Run this BEFORE starting the Spring Boot application.
-- ============================================================

CREATE DATABASE IF NOT EXISTS ms_admissions_engine
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ms_admissions_engine;

-- ------------------------------------------------------------
-- Table 1: COUNTRY_FRAMEWORKS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS country_frameworks (
    framework_id      INT           AUTO_INCREMENT PRIMARY KEY,
    region            VARCHAR(20)   NOT NULL,
    gpa_scale         DECIMAL(4,2)  NOT NULL,
    gpa_label         VARCHAR(50)   NOT NULL,
    primary_test      VARCHAR(30),
    language_req      VARCHAR(50),
    ects_required     BOOLEAN       DEFAULT FALSE,
    research_proposal BOOLEAN       DEFAULT FALSE,
    notes             TEXT,
    created_at        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_region CHECK (region IN ('US_CANADA', 'EUROPE', 'JAPAN'))
);

-- ------------------------------------------------------------
-- Table 2: UNIVERSITY_PROGRAMS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS university_programs (
    program_id          INT           AUTO_INCREMENT PRIMARY KEY,
    university_name     VARCHAR(200)  NOT NULL,
    program_name        VARCHAR(200)  NOT NULL,
    country             VARCHAR(100)  NOT NULL,
    framework_id        INT           NOT NULL,
    region              VARCHAR(20)   NOT NULL,
    degree_type         VARCHAR(20)   DEFAULT 'MS',
    specialization      VARCHAR(100),
    threshold_gpa       DECIMAL(4,2),
    threshold_gre_v     INT,
    threshold_gre_q     INT,
    threshold_gre_aw    DECIMAL(3,1),
    threshold_jlpt      VARCHAR(5),
    threshold_ielts     DECIMAL(3,1),
    threshold_toefl     INT,
    threshold_ects_cs   INT,
    thesis_required     BOOLEAN       DEFAULT FALSE,
    acceptance_rate     DECIMAL(5,2),
    avg_accepted_gpa    DECIMAL(4,2),
    scrape_source_url   VARCHAR(500),
    last_scraped_at     TIMESTAMP     NULL,
    tuition_annual_usd         DECIMAL(10,2),
    living_cost_annual_usd     DECIMAL(10,2),
    blocked_account_req_usd    DECIMAL(10,2),
    post_study_visa_months     INT,
    visa_program_name          VARCHAR(255),
    scholarship_types          VARCHAR(500),
    created_at          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_up_framework FOREIGN KEY (framework_id)
        REFERENCES country_frameworks(framework_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE KEY uq_university_program (university_name, program_name),
    INDEX idx_region (region),
    INDEX idx_specialization (specialization),
    INDEX idx_country (country)
);

-- ------------------------------------------------------------
-- Table 3: SCORING_WEIGHTS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS scoring_weights (
    weight_id           INT           AUTO_INCREMENT PRIMARY KEY,
    framework_id        INT           NOT NULL,
    weight_version      VARCHAR(20)   DEFAULT '1.0',
    weight_gpa          DECIMAL(5,4)  NOT NULL DEFAULT 0.0,
    weight_gre_verbal   DECIMAL(5,4)  NOT NULL DEFAULT 0.0,
    weight_gre_quant    DECIMAL(5,4)  NOT NULL DEFAULT 0.0,
    weight_gre_aw       DECIMAL(5,4)  NOT NULL DEFAULT 0.0,
    weight_language     DECIMAL(5,4)  NOT NULL DEFAULT 0.0,
    weight_ects         DECIMAL(5,4)  NOT NULL DEFAULT 0.0,
    weight_research     DECIMAL(5,4)  NOT NULL DEFAULT 0.0,
    weight_coursework   DECIMAL(5,4)  NOT NULL DEFAULT 0.0,
    weight_thesis       DECIMAL(5,4)  NOT NULL DEFAULT 0.0,
    is_active           BOOLEAN       DEFAULT TRUE,
    notes               TEXT,
    CONSTRAINT fk_sw_framework FOREIGN KEY (framework_id)
        REFERENCES country_frameworks(framework_id),
    INDEX idx_active_weights (framework_id, is_active)
);

-- Weight sum validation trigger
DELIMITER $$
CREATE TRIGGER trg_validate_weights_insert
BEFORE INSERT ON scoring_weights
FOR EACH ROW
BEGIN
    DECLARE total DECIMAL(6,4);
    SET total = NEW.weight_gpa + NEW.weight_gre_verbal + NEW.weight_gre_quant
              + NEW.weight_gre_aw + NEW.weight_language + NEW.weight_ects
              + NEW.weight_research + NEW.weight_coursework + NEW.weight_thesis;
    IF ABS(total - 1.0) > 0.001 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'scoring_weights components must sum to 1.0';
    END IF;
END$$

CREATE TRIGGER trg_validate_weights_update
BEFORE UPDATE ON scoring_weights
FOR EACH ROW
BEGIN
    DECLARE total DECIMAL(6,4);
    SET total = NEW.weight_gpa + NEW.weight_gre_verbal + NEW.weight_gre_quant
              + NEW.weight_gre_aw + NEW.weight_language + NEW.weight_ects
              + NEW.weight_research + NEW.weight_coursework + NEW.weight_thesis;
    IF ABS(total - 1.0) > 0.001 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'scoring_weights components must sum to 1.0';
    END IF;
END$$
DELIMITER ;

-- ------------------------------------------------------------
-- Table 4: APPLICANT_PROFILES
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS applicant_profiles (
    applicant_id        INT           AUTO_INCREMENT PRIMARY KEY,
    full_name           VARCHAR(200)  NOT NULL,
    email               VARCHAR(200)  UNIQUE,
    cgpa_10pt           DECIMAL(4,2)  NOT NULL,
    cgpa_scale          DECIMAL(4,2)  NOT NULL DEFAULT 10.0,
    university_name     VARCHAR(200),
    degree              VARCHAR(100)  DEFAULT 'B.Tech',
    specialization      VARCHAR(100)  DEFAULT 'Computer Science',
    graduation_year     INT,
    gre_verbal          INT,
    gre_quant           INT,
    gre_aw              DECIMAL(3,1),
    gmat_score          INT,
    toefl_score         INT,
    ielts_score         DECIMAL(3,1),
    jlpt_level          VARCHAR(5),
    ects_credits_cs     INT,
    thesis_completed    BOOLEAN       DEFAULT FALSE,
    research_proposal   TEXT,
    research_score      DECIMAL(5,2),
    sop_score           DECIMAL(5,2),
    publications_count  INT           DEFAULT 0,
    internship_months   INT           DEFAULT 0,
    annual_budget_usd         DECIMAL(10,2),
    liquid_savings_usd        DECIMAL(10,2),
    funding_preference        VARCHAR(50)   DEFAULT 'PARTIAL_SCHOLARSHIP',
    post_study_work_priority  VARCHAR(50)   DEFAULT 'HIGH',
    created_at          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_cgpa        CHECK (cgpa_10pt BETWEEN 0.0 AND 10.0),
    CONSTRAINT chk_gre_verbal  CHECK (gre_verbal  IS NULL OR gre_verbal  BETWEEN 130 AND 170),
    CONSTRAINT chk_gre_quant   CHECK (gre_quant   IS NULL OR gre_quant   BETWEEN 130 AND 170),
    CONSTRAINT chk_gre_aw      CHECK (gre_aw      IS NULL OR gre_aw      BETWEEN 0.0 AND 6.0),
    CONSTRAINT chk_jlpt        CHECK (jlpt_level  IS NULL OR jlpt_level  IN ('N1','N2','N3','N4','N5'))
);

-- ------------------------------------------------------------
-- Table 5: COMPATIBILITY_RESULTS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS compatibility_results (
    result_id            INT           AUTO_INCREMENT PRIMARY KEY,
    applicant_id         INT           NOT NULL,
    program_id           INT           NOT NULL,
    calculation_date     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    normalized_gpa       DECIMAL(5,2),
    normalized_gre_v     DECIMAL(5,2),
    normalized_gre_q     DECIMAL(5,2),
    normalized_gre_aw    DECIMAL(5,2),
    normalized_language  DECIMAL(5,2),
    normalized_ects      DECIMAL(5,2),
    normalized_research  DECIMAL(5,2),
    compatibility_score  DECIMAL(5,2)  NOT NULL,
    compatibility_tier   VARCHAR(20),
    financial_score      DECIMAL(5,2),
    visa_score           DECIMAL(5,2),
    holistic_score       DECIMAL(5,2),
    holistic_tier        VARCHAR(20),
    scholarship_eligible BOOLEAN       DEFAULT FALSE,
    matched_scholarships JSON,
    financial_summary    TEXT,
    visa_summary         TEXT,
    deficit_report       JSON,
    recommendations      TEXT,
    CONSTRAINT fk_cr_applicant FOREIGN KEY (applicant_id)
        REFERENCES applicant_profiles(applicant_id) ON DELETE CASCADE,
    CONSTRAINT fk_cr_program FOREIGN KEY (program_id)
        REFERENCES university_programs(program_id) ON DELETE CASCADE,
    INDEX idx_applicant_score (applicant_id, compatibility_score DESC),
    INDEX idx_tier (compatibility_tier)
);

-- ============================================================
-- SEED DATA
-- ============================================================

-- Country Frameworks
INSERT INTO country_frameworks (region, gpa_scale, gpa_label, primary_test, language_req, ects_required, research_proposal)
VALUES
    ('US_CANADA', 4.00, 'US 4.0 GPA',  'GRE',  'TOEFL/IELTS', FALSE, FALSE),
    ('EUROPE',    4.00, 'ECTS Grade',   NULL,   'IELTS',       TRUE,  FALSE),
    ('JAPAN',     4.00, 'Japanese 4.0', 'JLPT', 'JLPT/TOEFL',  FALSE, TRUE);

-- Scoring Weights (framework_id = 1: US_CANADA)
-- GPA 30%, GRE-Q 25%, GRE-V 15%, GRE-AW 5%, Language 10%, Coursework 10%, Thesis 5%
INSERT INTO scoring_weights
    (framework_id, weight_gpa, weight_gre_verbal, weight_gre_quant, weight_gre_aw,
     weight_language, weight_ects, weight_research, weight_coursework, weight_thesis, notes)
VALUES
    (1, 0.30, 0.15, 0.25, 0.05, 0.10, 0.00, 0.00, 0.10, 0.05, 'US/Canada default v1.0');

-- Scoring Weights (framework_id = 2: EUROPE)
-- GPA 25%, ECTS 30%, Language 15%, Thesis 20%, Coursework 10%
INSERT INTO scoring_weights
    (framework_id, weight_gpa, weight_gre_verbal, weight_gre_quant, weight_gre_aw,
     weight_language, weight_ects, weight_research, weight_coursework, weight_thesis, notes)
VALUES
    (2, 0.25, 0.00, 0.00, 0.00, 0.15, 0.30, 0.00, 0.10, 0.20, 'Europe ECTS default v1.0');

-- Scoring Weights (framework_id = 3: JAPAN)
-- GPA 20%, JLPT 35%, Research 30%, Coursework 10%, Thesis 5%
INSERT INTO scoring_weights
    (framework_id, weight_gpa, weight_gre_verbal, weight_gre_quant, weight_gre_aw,
     weight_language, weight_ects, weight_research, weight_coursework, weight_thesis, notes)
VALUES
    (3, 0.20, 0.00, 0.00, 0.00, 0.35, 0.00, 0.30, 0.10, 0.05, 'Japan MEXT default v1.0');

-- US/Canada Programs (framework_id = 1)
INSERT INTO university_programs
    (university_name, program_name, country, framework_id, region, specialization,
     threshold_gpa, threshold_gre_v, threshold_gre_q, threshold_gre_aw,
     threshold_toefl, threshold_ielts, acceptance_rate, avg_accepted_gpa,
     tuition_annual_usd, living_cost_annual_usd, blocked_account_req_usd,
     post_study_visa_months, visa_program_name, scholarship_types)
VALUES
    ('MIT', 'MS Electrical Engineering & CS', 'USA', 1, 'US_CANADA', 'CS/AI',         3.8, 163, 167, 5.0, 100, 7.5, 4.5,  3.90, 60000.0, 25000.0, 85000.0, 36, 'USA F-1 STEM OPT (36 Months)', 'Fulbright, Graduate TA/RA, MIT Presidential Fellowship'),
    ('Stanford University', 'MS Computer Science', 'USA', 1, 'US_CANADA', 'CS/AI',    3.7, 160, 165, 4.5, 100, 7.0, 6.0,  3.85, 62000.0, 26000.0, 88000.0, 36, 'USA F-1 STEM OPT (36 Months)', 'Fulbright, Graduate Assistantship, Knight-Hennessy Scholar'),
    ('CMU', 'MS Machine Learning', 'USA', 1, 'US_CANADA', 'AI/ML',                    3.75,162, 166, 4.5, 105, 7.5, 5.0,  3.88, 59000.0, 22000.0, 81000.0, 36, 'USA F-1 STEM OPT (36 Months)', 'Fulbright, Departmental TA/RA Appointments'),
    ('UC Berkeley', 'MEng EECS', 'USA', 1, 'US_CANADA', 'CS/AI',                     3.6, 158, 163, 4.0, 90,  7.0, 12.0, 3.75, 58000.0, 25000.0, 83000.0, 36, 'USA F-1 STEM OPT (36 Months)', 'Fulbright, Fung Institute Excellence Fellowship'),
    ('Cornell Tech', 'MS Computer Science', 'USA', 1, 'US_CANADA', 'CS',              3.5, 155, 160, 4.0, 90,  6.5, 15.0, 3.70, 64000.0, 27000.0, 91000.0, 36, 'USA F-1 STEM OPT (36 Months)', 'Fulbright, Cornell Tech Merit Scholarship'),
    ('Georgia Tech', 'MS Computer Science', 'USA', 1, 'US_CANADA', 'CS',              3.0, 153, 155, 3.5, 79,  6.5, 18.0, 3.55, 32000.0, 18000.0, 50000.0, 36, 'USA F-1 STEM OPT (36 Months)', 'Fulbright, Graduate Teaching/Research Assistantship (GRA/GTA)'),
    ('University of Toronto', 'MScAC', 'Canada', 1, 'US_CANADA', 'CS/AI',             3.5, 155, 160, 4.0, 93,  6.5, 12.0, 3.65, 35000.0, 18000.0, 15500.0, 36, 'Canada PGWP (36 Months Open Work Permit)', 'Vector Institute AI Scholarship, Ontario Graduate Scholarship (OGS)'),
    ('University of Waterloo', 'MASc CS', 'Canada', 1, 'US_CANADA', 'CS',             3.3, 150, 155, 3.5, 90,  6.5, 20.0, 3.50, 30000.0, 16000.0, 15500.0, 36, 'Canada PGWP (36 Months Open Work Permit)', 'International Master''s Award of Excellence (IMAE)');

-- European Programs (framework_id = 2)
INSERT INTO university_programs
    (university_name, program_name, country, framework_id, region, specialization,
     threshold_gpa, threshold_ielts, threshold_ects_cs, thesis_required, acceptance_rate, avg_accepted_gpa,
     tuition_annual_usd, living_cost_annual_usd, blocked_account_req_usd,
     post_study_visa_months, visa_program_name, scholarship_types)
VALUES
    ('TU Munich', 'MSc Informatics (AI)', 'Germany', 2, 'EUROPE', 'AI/ML',           3.3, 6.5, 90,  TRUE, 15.0, 3.40,  6500.0, 13000.0, 13000.0, 18, 'Germany 18-Month Jobseeker Visa / EU Blue Card Pathway', 'DAAD Master Studies Scholarship, Deutschlandstipendium'),
    ('ETH Zurich', 'MSc Computer Science', 'Switzerland', 2, 'EUROPE', 'CS',         3.7, 7.0, 120, TRUE, 8.0,  3.75,  1600.0, 24000.0, 24000.0, 6,  'Switzerland 6-Month Jobseeker Visa', 'ETH Excellence Scholarship & Opportunity Programme (ESOP)'),
    ('KTH Stockholm', 'MSc Machine Learning', 'Sweden', 2, 'EUROPE', 'AI/ML',        3.2, 6.5, 90,  FALSE,20.0, 3.30, 16500.0, 12500.0, 11500.0, 12, 'Sweden 12-Month Job Seeking Residence Permit', 'KTH Scholarship, Swedish Institute (SI) Scholarships'),
    ('TU Delft', 'MSc Computer Science', 'Netherlands', 2, 'EUROPE', 'CS',           3.0, 6.0, 80,  FALSE,22.0, 3.20, 22000.0, 14000.0, 14000.0, 12, 'Netherlands Zoekjaar / 1-Year Orientation Year', 'Justus & Louise van Effen Excellence Scholarship'),
    ('University of Amsterdam', 'MSc AI', 'Netherlands', 2, 'EUROPE', 'AI/ML',       3.2, 6.5, 90,  FALSE,18.0, 3.35, 23000.0, 15000.0, 14000.0, 12, 'Netherlands Zoekjaar / 1-Year Orientation Year', 'Amsterdam Merit Scholarship (AMS)'),
    ('University of Edinburgh', 'MSc AI', 'UK', 2, 'EUROPE', 'AI/ML',               3.3, 6.5, 80,  FALSE,15.0, 3.40, 42000.0, 18000.0, 16000.0, 24, 'UK Graduate Route (2-Year Unsponsored Work Visa)', 'Edinburgh Global Research Scholarship, Chevening Scholarship');

-- Japanese Programs (framework_id = 3)
INSERT INTO university_programs
    (university_name, program_name, country, framework_id, region, specialization,
     threshold_gpa, threshold_jlpt, threshold_toefl, thesis_required, acceptance_rate, avg_accepted_gpa,
     tuition_annual_usd, living_cost_annual_usd, blocked_account_req_usd,
     post_study_visa_months, visa_program_name, scholarship_types)
VALUES
    ('University of Tokyo', 'Graduate School of Information Science', 'Japan', 3, 'JAPAN', 'CS/AI', 3.2, 'N2', 80, TRUE, 25.0, 3.30, 3600.0, 12000.0, 14000.0, 24, 'Japan J-Find / Designated Activities (Up to 24 Months) + HSP Fast-Track', 'MEXT Japanese Government Scholarship, JASSO Honors, Todai Fellowship'),
    ('Kyoto University', 'Graduate School of Informatics', 'Japan', 3, 'JAPAN', 'CS/AI',   3.0, 'N2', 79, TRUE, 28.0, 3.10, 3600.0, 11000.0, 14000.0, 24, 'Japan J-Find / Designated Activities + HSP Points (PR in 1-3 Yrs)', 'MEXT Japanese Government Scholarship, JASSO Honors, Kyoto iUP'),
    ('Osaka University', 'Graduate School IS', 'Japan', 3, 'JAPAN', 'CS',                  2.8, 'N3', 70, TRUE, 30.0, 3.00, 3600.0, 10500.0, 14000.0, 24, 'Japan Designated Activities (Job Hunting) + HSP Points', 'MEXT Japanese Government Scholarship, JASSO Honors'),
    ('Tohoku University', 'Graduate School IS', 'Japan', 3, 'JAPAN', 'CS',                 2.8, 'N3', 70, TRUE, 32.0, 2.90, 3600.0,  9500.0, 14000.0, 24, 'Japan Designated Activities (Job Hunting) + HSP Points', 'MEXT Japanese Government Scholarship, JASSO Honors, President Fellowship'),
    ('NAIST', 'MS Information Science', 'Japan', 3, 'JAPAN', 'CS/AI',                     2.5, 'N3', 65, TRUE, 35.0, 2.80, 3600.0,  9000.0, 14000.0, 24, 'Japan Designated Activities (Job Hunting) + HSP Points', 'MEXT Japanese Government Scholarship, JASSO Honors, NAIST Foundation');
