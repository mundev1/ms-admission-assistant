"""
japan_scraper.py
----------------
Scraper for Japanese graduate programmes (MEXT / MIRAI-related).

Covered institutions
~~~~~~~~~~~~~~~~~~~~
1.  University of Tokyo — Graduate School of Information Science & Technology
2.  Kyoto University — Graduate School of Informatics
3.  Osaka University — Graduate School of Information Science & Technology
4.  Tohoku University — Graduate School of Information Sciences
5.  NAIST — Nara Institute of Science and Technology

Key characteristics of Japanese graduate admissions
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
* Most programmes require a **research proposal** and a supervising professor
  agreement before/during the application — ``thesis_required=True`` for all.
* **JLPT N2** is the standard Japanese-language threshold for non-native
  speakers enrolling in Japanese-medium programmes; N3 is occasionally
  accepted for English-track programmes at international-friendly schools.
* GPA is assessed on a Japanese 4.0 scale (A=4, B=3, C=2, D=1) or a
  percentage equivalent; threshold here is on the 4.0 scale.
* GRE is generally **not required** for Japanese programmes.
* TOEFL/IELTS thresholds apply to English-track / international applicants.

Scraping strategy is the same as the other regional scrapers:
live GET + BeautifulSoup, fallback to hardcoded data on failure.
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
        "university_name": "University of Tokyo",
        "program_name": "Master's Program — Graduate School of Information Science and Technology",
        "country": "JP",
        "region": "JAPAN",
        "specialization": "CS",
        "threshold_gpa": 3.0,
        "threshold_jlpt": "N2",
        "threshold_toefl": 79,
        "threshold_ielts": 6.0,
        "thesis_required": True,
        "acceptance_rate": 0.25,
        "avg_accepted_gpa": 3.4,
        "scrape_source_url": "https://www.i.u-tokyo.ac.jp/edu/entra/index_e.shtml",
    },
    {
        "university_name": "Kyoto University",
        "program_name": "Master's Program — Graduate School of Informatics",
        "country": "JP",
        "region": "JAPAN",
        "specialization": "CS",
        "threshold_gpa": 3.0,
        "threshold_jlpt": "N2",
        "threshold_toefl": 72,
        "threshold_ielts": 6.0,
        "thesis_required": True,
        "acceptance_rate": 0.30,
        "avg_accepted_gpa": 3.35,
        "scrape_source_url": "https://www.i.kyoto-u.ac.jp/en/admission/",
    },
    {
        "university_name": "Osaka University",
        "program_name": "Master's Program — Graduate School of Information Science and Technology",
        "country": "JP",
        "region": "JAPAN",
        "specialization": "CS",
        "threshold_gpa": 2.8,
        "threshold_jlpt": "N2",
        "threshold_toefl": 70,
        "threshold_ielts": 5.5,
        "thesis_required": True,
        "acceptance_rate": 0.35,
        "avg_accepted_gpa": 3.2,
        "scrape_source_url": "https://www.ist.osaka-u.ac.jp/english/admission/",
    },
    {
        "university_name": "Tohoku University",
        "program_name": "Master's Program — Graduate School of Information Sciences",
        "country": "JP",
        "region": "JAPAN",
        "specialization": "CS",
        "threshold_gpa": 2.8,
        "threshold_jlpt": "N2",
        "threshold_toefl": 72,
        "threshold_ielts": 5.5,
        "thesis_required": True,
        "acceptance_rate": 0.38,
        "avg_accepted_gpa": 3.1,
        "scrape_source_url": "https://www.is.tohoku.ac.jp/en/for_applicant/",
    },
    {
        "university_name": "Nara Institute of Science and Technology (NAIST)",
        "program_name": "Master's Program — Division of Information Science",
        "country": "JP",
        "region": "JAPAN",
        "specialization": "CS",
        # NAIST's IPADS English-track accepts N3; Japanese-medium requires N2
        "threshold_gpa": 2.7,
        "threshold_jlpt": "N3",
        "threshold_toefl": 68,
        "threshold_ielts": 5.5,
        "thesis_required": True,
        "acceptance_rate": 0.45,
        "avg_accepted_gpa": 3.0,
        "scrape_source_url": "https://www.naist.jp/en/prospective_students/apply/",
    },
]

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

_JLPT_PATTERN = re.compile(r"\bJLPT\b[^\w]*(N[1-5])\b", re.IGNORECASE)
_TOEFL_PATTERN = re.compile(
    r"TOEFL[^\d]*(\b(?:6[0-9]|[7-9]\d|1[01]\d|120)\b)", re.IGNORECASE
)
_IELTS_PATTERN = re.compile(r"IELTS[^\d]*(\b[5-9](?:\.\d)?\b)", re.IGNORECASE)
_GPA_PATTERN = re.compile(r"\b([23]\.\d)\b")


def _parse_jlpt(text: str) -> Optional[str]:
    """Extract JLPT level (N1–N5)."""
    match = _JLPT_PATTERN.search(text)
    return match.group(1).upper() if match else None


def _parse_toefl(text: str) -> Optional[int]:
    """Extract TOEFL iBT total score."""
    match = _TOEFL_PATTERN.search(text)
    return int(match.group(1)) if match else None


def _parse_ielts(text: str) -> Optional[float]:
    """Extract IELTS overall band score."""
    match = _IELTS_PATTERN.search(text)
    return float(match.group(1)) if match else None


def _parse_gpa(text: str) -> Optional[float]:
    """Extract a GPA threshold on a 4.0 scale."""
    match = _GPA_PATTERN.search(text)
    return float(match.group(1)) if match else None


# ---------------------------------------------------------------------------
# Scraper implementation
# ---------------------------------------------------------------------------

class JapanScraper(BaseScraper):
    """
    Scraper for Japanese graduate programmes related to MEXT / MIRAI schemes.

    All programmes require thesis research and have JLPT language thresholds.
    GRE fields are ``None`` (not required by Japanese institutions).
    """

    def scrape(self) -> list[ProgramData]:
        """
        Scrape Japanese programme data.

        Returns
        -------
        list[ProgramData]
            One entry per programme.
        """
        results: list[ProgramData] = []

        for entry in _FALLBACK_DATA:
            url: str = entry["scrape_source_url"]
            live_jlpt: Optional[str] = None
            live_toefl: Optional[int] = None
            live_ielts: Optional[float] = None
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

                live_jlpt = _parse_jlpt(page_text)
                live_toefl = _parse_toefl(page_text)
                live_ielts = _parse_ielts(page_text)
                live_gpa = _parse_gpa(page_text)
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

            # Merge: prefer live values, fill gaps from fallback
            jlpt = (live_jlpt or entry.get("threshold_jlpt")) if used_live else entry.get("threshold_jlpt")
            toefl = (live_toefl or entry.get("threshold_toefl")) if used_live else entry.get("threshold_toefl")
            ielts = (live_ielts or entry.get("threshold_ielts")) if used_live else entry.get("threshold_ielts")
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
                    threshold_gre_q=None,    # Not required
                    threshold_gre_v=None,
                    threshold_gre_aw=None,
                    threshold_jlpt=jlpt,
                    threshold_toefl=toefl,
                    threshold_ielts=ielts,
                    threshold_ects_cs=None,  # ECTS not used in Japan
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
    scraper = JapanScraper()
    programs = scraper.safe_scrape()
    print(f"\n{'='*70}")
    print(f"  Japan Scraper — {len(programs)} programme(s) found")
    print(f"{'='*70}")
    for p in programs:
        print(
            f"  {p.university_name}\n"
            f"    Programme  : {p.program_name}\n"
            f"    GPA ≥      : {p.threshold_gpa}\n"
            f"    JLPT ≥     : {p.threshold_jlpt}\n"
            f"    TOEFL ≥    : {p.threshold_toefl}\n"
            f"    IELTS ≥    : {p.threshold_ielts}\n"
            f"    Thesis     : {p.thesis_required}\n"
            f"    Accept rate: {p.acceptance_rate:.0%}\n"
            f"    Source URL : {p.scrape_source_url}\n"
        )
