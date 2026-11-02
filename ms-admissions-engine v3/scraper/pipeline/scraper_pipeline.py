"""
scraper_pipeline.py
-------------------
Orchestrates the three regional scrapers and validates / deduplicates the
collected :class:`~scrapers.base_scraper.ProgramData` records.

Typical usage
~~~~~~~~~~~~~
::

    from pipeline.scraper_pipeline import ScraperPipeline

    pipeline = ScraperPipeline()
    programs = pipeline.run_and_validate()

The pipeline is intentionally fault-tolerant: each scraper runs through its
own ``safe_scrape()`` wrapper, so a completely failed scraper contributes an
empty list rather than crashing the run.
"""

from __future__ import annotations

import logging
from typing import Optional

from scrapers.base_scraper import ProgramData
from scrapers.europe_scraper import EuropeScraper
from scrapers.japan_scraper import JapanScraper
from scrapers.us_scraper import USScraper

logger = logging.getLogger(__name__)


class ScraperPipeline:
    """
    Full scrape-deduplicate-validate pipeline.

    Parameters
    ----------
    timeout:
        HTTP timeout (seconds) forwarded to each scraper.
    regions:
        Subset of regions to run.  Pass ``None`` or ``["all"]`` to run all
        three scrapers.  Valid values: ``"us"``, ``"europe"``, ``"japan"``.
    """

    REGION_MAP: dict[str, type] = {
        "us": USScraper,
        "europe": EuropeScraper,
        "japan": JapanScraper,
    }

    def __init__(
        self,
        timeout: int = 15,
        regions: Optional[list[str]] = None,
    ) -> None:
        self.timeout = timeout
        if regions is None or regions == ["all"]:
            self._active_regions = list(self.REGION_MAP.keys())
        else:
            self._active_regions = [r.lower() for r in regions if r.lower() in self.REGION_MAP]
            if not self._active_regions:
                raise ValueError(
                    f"No valid regions in {regions!r}. "
                    f"Choose from: {list(self.REGION_MAP.keys())}."
                )

    # ------------------------------------------------------------------
    # Public API
    # ------------------------------------------------------------------

    def run_all(self) -> list[ProgramData]:
        """
        Run every active regional scraper and combine results.

        Returns
        -------
        list[ProgramData]
            Raw, combined list (may contain duplicates).
        """
        programs: list[ProgramData] = []

        for region_key in self._active_regions:
            scraper_cls = self.REGION_MAP[region_key]
            scraper = scraper_cls(timeout=self.timeout)
            logger.info("Running scraper: %s", scraper_cls.__name__)
            batch = scraper.safe_scrape()
            logger.info(
                "%s returned %d record(s).", scraper_cls.__name__, len(batch)
            )
            programs.extend(batch)

        logger.info("Total raw records collected: %d", len(programs))
        return programs

    def deduplicate(self, programs: list[ProgramData]) -> list[ProgramData]:
        """
        Remove duplicate records with the same ``(university_name, program_name)``
        composite key.

        When duplicates are found, the **first** occurrence is kept and
        subsequent ones are dropped (order is preserved from ``run_all``).

        Parameters
        ----------
        programs:
            Raw list that may contain duplicate entries.

        Returns
        -------
        list[ProgramData]
            Deduplicated list.
        """
        seen: set[tuple[str, str]] = set()
        unique: list[ProgramData] = []

        for prog in programs:
            key = (prog.university_name.strip().lower(), prog.program_name.strip().lower())
            if key in seen:
                logger.debug(
                    "Duplicate detected and dropped: %s — %s",
                    prog.university_name,
                    prog.program_name,
                )
                continue
            seen.add(key)
            unique.append(prog)

        dropped = len(programs) - len(unique)
        if dropped:
            logger.info("Deduplication removed %d duplicate record(s).", dropped)
        return unique

    def validate(self, program: ProgramData) -> bool:
        """
        Validate that a :class:`ProgramData` record has the minimum required fields.

        A record is considered **valid** if all of the following are non-empty:

        * ``university_name``
        * ``program_name``
        * ``country``
        * ``region``  (and one of the recognised values)
        * ``specialization``
        * ``scrape_source_url``

        Parameters
        ----------
        program:
            The record to validate.

        Returns
        -------
        bool
            ``True`` if valid, ``False`` otherwise.
        """
        valid_regions = {"US_CANADA", "EUROPE", "JAPAN"}

        if not program.university_name or not program.university_name.strip():
            logger.warning("Validation failed: missing university_name for %r", program)
            return False

        if not program.program_name or not program.program_name.strip():
            logger.warning("Validation failed: missing program_name for %r", program)
            return False

        if not program.country or not program.country.strip():
            logger.warning(
                "Validation failed: missing country for %s — %s",
                program.university_name,
                program.program_name,
            )
            return False

        if program.region not in valid_regions:
            logger.warning(
                "Validation failed: invalid region '%s' for %s — %s",
                program.region,
                program.university_name,
                program.program_name,
            )
            return False

        if not program.specialization or not program.specialization.strip():
            logger.warning(
                "Validation failed: missing specialization for %s — %s",
                program.university_name,
                program.program_name,
            )
            return False

        if not program.scrape_source_url or not program.scrape_source_url.strip():
            logger.warning(
                "Validation failed: missing scrape_source_url for %s — %s",
                program.university_name,
                program.program_name,
            )
            return False

        return True

    def run_and_validate(self) -> list[ProgramData]:
        """
        Full pipeline: scrape → deduplicate → validate.

        Returns
        -------
        list[ProgramData]
            Clean, validated, deduplicated programme records.
        """
        raw = self.run_all()
        deduped = self.deduplicate(raw)

        valid: list[ProgramData] = []
        invalid_count = 0

        for prog in deduped:
            if self.validate(prog):
                valid.append(prog)
            else:
                invalid_count += 1

        logger.info(
            "Pipeline complete — valid: %d, invalid/dropped: %d",
            len(valid),
            invalid_count,
        )
        return valid
