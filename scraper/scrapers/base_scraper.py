"""
base_scraper.py
---------------
Abstract base class and shared data model for all MS-admissions scrapers.

Every concrete scraper must:
  1. Inherit from ``BaseScraper``.
  2. Implement the ``scrape()`` method, which returns a list of ``ProgramData``.

Use ``safe_scrape()`` in orchestration code so that a single failing scraper
never aborts the whole pipeline.
"""

from __future__ import annotations

import logging
from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from typing import Optional

logger = logging.getLogger(__name__)


# ---------------------------------------------------------------------------
# Data model
# ---------------------------------------------------------------------------

@dataclass
class ProgramData:
    """
    Normalised representation of a single graduate programme entry.

    Threshold fields represent the **minimum / typical** cut-off values
    reported by the institution.  ``None`` means the institution did not
    publish a threshold (or one is not applicable for that region).

    Attributes
    ----------
    university_name:
        Full official name of the institution.
    program_name:
        Official programme title (e.g. ``"MSc Artificial Intelligence"``).
    country:
        ISO 3166-1 alpha-2 country code (e.g. ``"US"``, ``"DE"``, ``"JP"``).
    region:
        Logical grouping used internally: ``US_CANADA``, ``EUROPE``, or ``JAPAN``.
    specialization:
        Broad field focus (e.g. ``"AI"``, ``"ML"``, ``"CS"``, ``"Robotics"``).
    degree_type:
        Degree awarded; defaults to ``"MS"``.
    threshold_gpa:
        Minimum GPA on a 4.0 scale (US/Canada) or 1.0–5.0 scale (EU/Japan).
    threshold_gre_q:
        Minimum GRE Quantitative score (130–170).
    threshold_gre_v:
        Minimum GRE Verbal score (130–170).
    threshold_gre_aw:
        Minimum GRE Analytical Writing score (0.0–6.0).
    threshold_jlpt:
        Minimum JLPT level required (e.g. ``"N2"``).
    threshold_ielts:
        Minimum IELTS overall band score.
    threshold_toefl:
        Minimum TOEFL iBT total score.
    threshold_ects_cs:
        Minimum ECTS credits in CS-related coursework required for admission.
    thesis_required:
        Whether a research thesis / dissertation is mandatory.
    acceptance_rate:
        Reported or estimated acceptance rate as a fraction (0.0–1.0).
    avg_accepted_gpa:
        Average GPA of accepted students (same scale as ``threshold_gpa``).
    scrape_source_url:
        The URL from which this record was scraped or verified.
    """

    university_name: str
    program_name: str
    country: str
    region: str                          # US_CANADA | EUROPE | JAPAN
    specialization: str
    degree_type: str = "MS"

    # --- Academic score thresholds ---
    threshold_gpa: Optional[float] = None
    threshold_gre_q: Optional[int] = None
    threshold_gre_v: Optional[int] = None
    threshold_gre_aw: Optional[float] = None

    # --- Language / qualification thresholds ---
    threshold_jlpt: Optional[str] = None
    threshold_ielts: Optional[float] = None
    threshold_toefl: Optional[int] = None
    threshold_ects_cs: Optional[int] = None

    # --- Programme characteristics ---
    thesis_required: bool = False
    acceptance_rate: Optional[float] = None
    avg_accepted_gpa: Optional[float] = None

    # --- Provenance ---
    scrape_source_url: str = ""

    # ------------------------------------------------------------------
    # Helpers
    # ------------------------------------------------------------------

    def __str__(self) -> str:  # noqa: D105
        return (
            f"[{self.region}] {self.university_name} — {self.program_name} "
            f"(GPA≥{self.threshold_gpa}, GRE-Q≥{self.threshold_gre_q})"
        )


# ---------------------------------------------------------------------------
# Abstract base scraper
# ---------------------------------------------------------------------------

class BaseScraper(ABC):
    """
    Abstract base class that every regional scraper must extend.

    Subclasses implement :meth:`scrape`; callers should prefer
    :meth:`safe_scrape` so that network or parsing errors are caught and
    logged gracefully without crashing the pipeline.

    Parameters
    ----------
    timeout:
        HTTP request timeout in seconds passed to ``requests.get``.
    headers:
        HTTP headers sent with every request.  Defaults to a desktop
        Chrome User-Agent to avoid bot-detection blocks.
    """

    DEFAULT_HEADERS: dict[str, str] = {
        "User-Agent": (
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/118.0.0.0 Safari/537.36"
        ),
        "Accept-Language": "en-US,en;q=0.9",
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    }

    def __init__(
        self,
        timeout: int = 15,
        headers: Optional[dict[str, str]] = None,
    ) -> None:
        self.timeout = timeout
        self.headers: dict[str, str] = headers or dict(self.DEFAULT_HEADERS)
        self.logger = logging.getLogger(self.__class__.__name__)

    @abstractmethod
    def scrape(self) -> list[ProgramData]:
        """
        Perform the scrape and return a list of :class:`ProgramData` objects.

        Raises
        ------
        Exception
            Any network / parsing error.  Callers should use
            :meth:`safe_scrape` to handle these gracefully.
        """
        ...

    def safe_scrape(self) -> list[ProgramData]:
        """
        Wrapper around :meth:`scrape` that catches all exceptions.

        Returns an empty list on failure so the pipeline can continue
        with other scrapers.

        Returns
        -------
        list[ProgramData]
            Successfully scraped programmes, or ``[]`` on error.
        """
        try:
            results = self.scrape()
            self.logger.info(
                "%s scraped %d programme(s) successfully.",
                self.__class__.__name__,
                len(results),
            )
            return results
        except Exception as exc:  # noqa: BLE001
            self.logger.error(
                "%s encountered an error during scraping: %s",
                self.__class__.__name__,
                exc,
                exc_info=True,
            )
            return []
