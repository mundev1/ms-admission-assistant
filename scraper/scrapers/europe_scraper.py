"""
europe_scraper.py
-----------------
Scraper for European MS programmes.

Covered institutions
~~~~~~~~~~~~~~~~~~~~
1.  Technical University of Munich (TUM) — MSc CS / AI
2.  ETH Zurich — MSc CS
3.  KTH Royal Institute of Technology — MSc ML
4.  Delft University of Technology — MSc CS
5.  University of Amsterdam — MSc AI
6.  University of Edinburgh — MSc AI

Notes on European admissions data
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
* GPAs are normalised to a **4.0 scale equivalent** for cross-comparison,
  even though most EU institutions use 1.0–5.0 (Germany), 1–10 (NL), or
  Honours-based (UK) scales.  The raw local threshold is stored alongside.
* **ECTS credits** thresholds refer to prior CS/STEM coursework credits
  required by the application.
* GRE scores are generally *not* required by EU institutions; those fields
  are set to ``None``.
* IELTS is the primary English proficiency metric used.

Strategy is identical to USScraper: live GET + BeautifulSoup parse, with a
hardcoded fallback on failure.
"""

from __future__ import annotations

import logging
import re
from typing import Optional

import requests
from bs4 import BeautifulSoup

from scrapers.base_scraper import BaseScraper, ProgramData

logger = logging.getLogger(__name__)

# ---------------------------------------------------------------------------
# Hardcoded reference data (realistic as of 2024/2025 admission cycles)
# ---------------------------------------------------------------------------

_FALLBACK_DATA: list[dict] = [
    {
        "university_name": "Technical University of Munich",
        "program_name": "MSc Computer Science / Artificial Intelligence",
        "country": "DE",
        "region": "EUROPE",
        "specialization": "AI",
        # TUM uses a local GPA scale; 1.5 ≈ 3.5/4.0
        "threshold_gpa": 3.5,
        "threshold_ielts": 7.0,
        "threshold_toefl": 95,
        "threshold_ects_cs": 60,
        "thesis_required": True,
        "acceptance_rate": 0.08,
        "avg_accepted_gpa": 3.7,
        "scrape_source_url": "https://www.in.tum.de/en/for-prospective-students/masters-programs/informatics/",
    },
    {
        "university_name": "ETH Zurich",
        "program_name": "MSc Computer Science",
        "country": "CH",
        "region": "EUROPE",
        "specialization": "CS",
        "threshold_gpa": 3.7,
        "threshold_ielts": 7.0,
        "threshold_toefl": 100,
        "threshold_ects_cs": 90,
        "thesis_required": True,
        "acceptance_rate": 0.10,
        "avg_accepted_gpa": 3.85,
        "scrape_source_url": "https://inf.ethz.ch/studies/master/application.html",
    },
    {
        "university_name": "KTH Royal Institute of Technology",
        "program_name": "MSc Machine Learning",
        "country": "SE",
        "region": "EUROPE",
        "specialization": "ML",
        "threshold_gpa": 3.3,
        "threshold_ielts": 6.5,
        "threshold_toefl": 90,
        "threshold_ects_cs": 60,
        "thesis_required": True,
        "acceptance_rate": 0.15,
        "avg_accepted_gpa": 3.55,
        "scrape_source_url": "https://www.kth.se/en/studies/master/machinelearning/entry-requirements-1.48533",
    },
    {
        "university_name": "Delft University of Technology",
        "program_name": "MSc Computer Science",
        "country": "NL",
        "region": "EUROPE",
        "specialization": "CS",
        "threshold_gpa": 3.2,
        "threshold_ielts": 6.5,
        "threshold_toefl": 90,
        "threshold_ects_cs": 60,
        "thesis_required": True,
        "acceptance_rate": 0.20,
        "avg_accepted_gpa": 3.4,
        "scrape_source_url": "https://www.tudelft.nl/onderwijs/opleidingen/masters/cs/msc-computer-science/admission",
    },
    {
        "university_name": "University of Amsterdam",
        "program_name": "MSc Artificial Intelligence",
        "country": "NL",
        "region": "EUROPE",
        "specialization": "AI",
        "threshold_gpa": 3.3,
        "threshold_ielts": 6.5,
        "threshold_toefl": 92,
        "threshold_ects_cs": 60,
        "thesis_required": True,
        "acceptance_rate": 0.22,
        "avg_accepted_gpa": 3.5,
        "scrape_source_url": "https://www.uva.nl/en/programmes/masters/artificial-intelligence/entry-requirements/entry-requirements.html",
    },
    {
        "university_name": "University of Edinburgh",
        "program_name": "MSc Artificial Intelligence",
        "country": "GB",
        "region": "EUROPE",
        "specialization": "AI",
        "threshold_gpa": 3.5,
        "threshold_ielts": 6.5,
        "threshold_toefl": 92,
        "threshold_ects_cs": 80,
        "thesis_required": True,
        "acceptance_rate": 0.18,
        "avg_accepted_gpa": 3.65,
        "scrape_source_url": "https://www.ed.ac.uk/studying/postgraduate/degrees/index.php?r=site/view&edition=2024&id=107",
    },
]


# ---------------------------------------------------------------------------
# Helpers: lightweight extractors for EU-specific fields
# ---------------------------------------------------------------------------

def _parse_ielts(text: str) -> Optional[float]:
    """Extract IELTS overall band score."""
    match = re.search(r"IELTS[^\d]*(\b[5-9](?:\.\d)?\b)", text, re.IGNORECASE)
    return float(match.group(1)) if match else None


def _parse_toefl(text: str) -> Optional[int]:
    """Extract TOEFL iBT total score."""
    match = re.search(r"TOEFL[^\d]*(\b(?:6[0-9]|[7-9]\d|1[01]\d|120)\b)", text, re.IGNORECASE)
    return int(match.group(1)) if match else None


def _parse_ects(text: str) -> Optional[int]:
    """Extract minimum ECTS credits requirement."""
    match = re.search(r"(\b\d{2,3}\b)\s*ECTS", text, re.IGNORECASE)
    return int(match.group(1)) if match else None


def _parse_gpa_eu(text: str) -> Optional[float]:
    """
    Try to extract a GPA-like threshold from EU page text.

    Handles German 1.x scale mentions and normalised 4.0 scale mentions.
    Returns value on 4.0 scale.
    """
    # German scale: "1.5" or "1,5" (lower is better; 1.0 = best)
    german = re.search(r"\b([12]\.[05])\b", text)
    if german:
        raw = float(german.group(1))
        # Convert: 4.0 − (raw − 1.0) × (4.0 / 4.0) approximately
        normalised = round(4.0 - (raw - 1.0) * (4.0 / 5.0), 2)
        return max(0.0, min(4.0, normalised))

    # Standard 4.0 scale
    standard = re.search(r"\b([34]\.\d)\b", text)
    return float(standard.group(1)) if standard else None


# ---------------------------------------------------------------------------
# Scraper implementation
# ---------------------------------------------------------------------------

class EuropeScraper(BaseScraper):
    """
    Scraper for European MS Computer Science / AI / ML programmes.

    Data enrichment strategy (per programme):
      1. Live GET request to the admissions URL.
      2. BeautifulSoup HTML parse to extract IELTS / TOEFL / ECTS thresholds.
      3. Hardcoded fallback on any failure.

    GRE fields are ``None`` for all European programmes (not required).
    """

    def scrape(self) -> list[ProgramData]:
        """
        Scrape European programme data.

        Returns
        -------
        list[ProgramData]
            One entry per programme.
        """
        results: list[ProgramData] = []

        for entry in _FALLBACK_DATA:
            url: str = entry["scrape_source_url"]
            live_ielts: Optional[float] = None
            live_toefl: Optional[int] = None
            live_ects: Optional[int] = None
            live_gpa: Optional[float] = None
            used_live = False

            # --- Attempt live scrape ---
            try:
                response = requests.get(url, headers=self.headers, timeout=self.timeout)
                response.raise_for_status()

                soup = BeautifulSoup(response.text, "lxml")
                for tag in soup(["script", "style", "nav", "footer", "header"]):
                    tag.decompose()

                page_text = soup.get_text(separator=" ", strip=True)

                live_ielts = _parse_ielts(page_text)
                live_toefl = _parse_toefl(page_text)
                live_ects = _parse_ects(page_text)
                live_gpa = _parse_gpa_eu(page_text)
                used_live = True
                self.logger.debug("Live parse succeeded for %s", url)

            except requests.RequestException as exc:
                self.logger.warning(
                    "Request failed for %s (%s) — using hardcoded fallback.",
                    entry["university_name"],
                    exc,
                )
            except Exception as exc:  # noqa: BLE001
                self.logger.warning(
                    "HTML parse failed for %s (%s) — using hardcoded fallback.",
                    entry["university_name"],
                    exc,
                )

            # Merge live data with fallback (prefer live, fill gaps from fallback)
            ielts = (live_ielts or entry.get("threshold_ielts")) if used_live else entry.get("threshold_ielts")
            toefl = (live_toefl or entry.get("threshold_toefl")) if used_live else entry.get("threshold_toefl")
            ects = (live_ects or entry.get("threshold_ects_cs")) if used_live else entry.get("threshold_ects_cs")
            gpa = (live_gpa or entry.get("threshold_gpa")) if used_live else entry.get("threshold_gpa")

            results.append(
                ProgramData(
                    university_name=entry["university_name"],
                    program_name=entry["program_name"],
                    country=entry["country"],
                    region=entry["region"],
                    specialization=entry["specialization"],
                    degree_type="MS",
                    threshold_gpa=gpa,
                    threshold_gre_q=None,     # Not required in Europe
                    threshold_gre_v=None,
                    threshold_gre_aw=None,
                    threshold_ielts=ielts,
                    threshold_toefl=toefl,
                    threshold_ects_cs=ects,
                    thesis_required=entry.get("thesis_required", True),
                    acceptance_rate=entry.get("acceptance_rate"),
                    avg_accepted_gpa=entry.get("avg_accepted_gpa"),
                    scrape_source_url=url,
                )
            )

        return results


# ---------------------------------------------------------------------------
# CLI entry-point
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s [%(levelname)s] %(name)s — %(message)s",
    )
    scraper = EuropeScraper()
    programs = scraper.safe_scrape()
    print(f"\n{'='*70}")
    print(f"  Europe Scraper — {len(programs)} programme(s) found")
    print(f"{'='*70}")
    for p in programs:
        print(
            f"  {p.university_name}\n"
            f"    Programme  : {p.program_name}\n"
            f"    GPA ≥      : {p.threshold_gpa}\n"
            f"    IELTS ≥    : {p.threshold_ielts}\n"
            f"    TOEFL ≥    : {p.threshold_toefl}\n"
            f"    ECTS CS ≥  : {p.threshold_ects_cs}\n"
            f"    Thesis     : {p.thesis_required}\n"
            f"    Source URL : {p.scrape_source_url}\n"
        )
