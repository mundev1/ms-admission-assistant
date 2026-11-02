# Cross-Border MS Admissions Matrix Engine

A decision-support system that calculates a dynamic **compatibility score** between a student's academic profile (B.Tech in CS/AI) and international Master's programs across the US, Canada, Europe, and Japan.

## Project Structure

```
ms-admissions-engine/
├── backend/          # Java 17 Spring Boot REST API + Scoring Engine
├── scraper/          # Python 3.9 web scrapers (BeautifulSoup + Selenium)
├── frontend/         # HTML/CSS/JS Dashboard (no frameworks)
└── database/         # MySQL schema + seed data
```

## Quick Start

### 1. Database Setup
```bash
mysql -u root -p < database/schema.sql
```

### 2. Backend (Java Spring Boot)
```bash
cd backend
# Edit src/main/resources/application.properties with your MySQL credentials
mvn spring-boot:run
# API available at http://localhost:8080
```

### 3. Python Scraper
```bash
cd scraper
pip install -r requirements.txt
python main.py --host localhost --user root --password <pwd> --database ms_admissions_engine
# Use --dry-run to preview without writing to DB
```

### 4. Frontend
```bash
# Open frontend/index.html directly in a browser
# (No build step needed — pure HTML/CSS/JS)
open frontend/index.html
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/applicants` | Save applicant profile (idempotent upsert) |
| `GET` | `/api/applicants/{id}` | Get applicant by ID |
| `GET` | `/api/programs` | List all university programs |
| `POST` | `/api/score/region/{region}` | Score against all programs in a region (`ALL`, `US`, `EU`, `JP`) |
| `POST` | `/api/score/program/{programId}` | Score against a specific program |

**Regions:** `ALL`, `US_CANADA` (`US`), `EUROPE` (`EU`), `JAPAN` (`JP`)

## Holistic Scoring & Feasibility Overlay

In addition to academic compatibility, the engine computes a **Holistic Feasibility Index** combining:
- **Academic Fit (50%)**: Normalized GPA, GRE percentiles, ECTS coursework, and research composite.
- **Financial Feasibility (30%)**: Annual net cost of attendance vs declared budget, mandatory blocked accounts (e.g. Germany Sperrkonto at €11,904 / ~$13k USD), and pre-matched scholarship eligibility (MEXT, DAAD, Fulbright, TA/RA).
- **Post-Study Visa Outlook (20%)**: Visa pathways and work authorization duration (e.g. US 36-month STEM OPT, Germany 18-month Jobseeker / EU Blue Card, Japan 24-month J-Find / Designated Activities).

## Scoring Formula

Each region applies a different weighted formula:

| Component | US/Canada | Europe | Japan |
|-----------|-----------|--------|-------|
| GPA | 30% | 25% | 20% |
| GRE Quant | 25% | — | — |
| GRE Verbal | 15% | — | — |
| GRE AW | 5% | — | — |
| Language (TOEFL/IELTS/JLPT) | 10% | 15% | **35%** |
| ECTS Credits | — | **30%** | — |
| Research Proposal | — | — | **30%** |
| Coursework | 10% | 10% | 10% |
| Thesis | 5% | 20% | 5% |

## Compatibility Tiers

| Score | Tier | Meaning |
|-------|------|---------|
| ≥ 80% | 🟢 SAFE | Strong match, high acceptance probability |
| 65–79% | 🟡 MATCH | Good fit, competitive application |
| 50–64% | 🟠 REACH | Possible with strong SOP/recommendations |
| < 50% | 🔴 UNLIKELY | Significant profile gaps present |

## Tech Stack
- **Backend:** Java 17, Spring Boot 3.2, Spring Data JPA
- **Database:** MySQL 8.x
- **Scraper:** Python 3.9, BeautifulSoup4, Selenium, mysql-connector-python
- **Frontend:** HTML5, CSS3, Vanilla JavaScript (ES6+)
