"""
main.py
-------
Entry-point for the Cross-Border MS Admissions Matrix Engine scraper.

CLI usage
~~~~~~~~~
::

    # Full run, write to MySQL
    python main.py --host localhost --user root --password secret --database admissions

    # Dry-run (no DB write) for all regions
    python main.py --dry-run

    # Scrape only European programmes
    python main.py --dry-run --region europe

    # Scrape only US/Canada, write to DB
    python main.py --host localhost --user root --password secret \\
                   --database admissions --region us

    # Scrape Japan + US/Canada together
    python main.py --dry-run --region us --region japan

Arguments
~~~~~~~~~
--host        MySQL server hostname (default: localhost).
--port        MySQL port (default: 3306).
--user        MySQL username.
--password    MySQL password.
--database    MySQL database / schema name.
--dry-run     Print collected data instead of writing to DB.
--region      Region(s) to scrape: ``us``, ``europe``, ``japan``, ``all``.
              May be supplied multiple times (e.g. ``--region us --region japan``).
              Defaults to ``all``.
--timeout     HTTP request timeout in seconds (default: 15).
--log-level   Logging verbosity: DEBUG, INFO, WARNING, ERROR (default: INFO).
"""

from __future__ import annotations

import argparse
import logging
import sys
from typing import Optional

from pipeline.db_writer import DatabaseWriter
from pipeline.scraper_pipeline import ScraperPipeline
from scrapers.base_scraper import ProgramData


# ---------------------------------------------------------------------------
# Logging setup
# ---------------------------------------------------------------------------

def _configure_logging(level_name: str) -> None:
    """Configure root logger with a human-readable format."""
    numeric_level = getattr(logging, level_name.upper(), logging.INFO)
    logging.basicConfig(
        level=numeric_level,
        format="%(asctime)s [%(levelname)-8s] %(name)s — %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S",
        stream=sys.stdout,
    )


# ---------------------------------------------------------------------------
# Argument parsing
# ---------------------------------------------------------------------------

def _build_parser() -> argparse.ArgumentParser:
    """Build and return the CLI argument parser."""
    parser = argparse.ArgumentParser(
        prog="ms-admissions-engine",
        description=(
            "Cross-Border MS Admissions Matrix Engine — "
            "scrapes graduate programme data from US/Canada, Europe, and Japan."
        ),
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )

    # MySQL connection args
    db_group = parser.add_argument_group("MySQL connection")
    db_group.add_argument(
        "--host",
        default="localhost",
        metavar="HOST",
        help="MySQL server hostname (default: %(default)s).",
    )
    db_group.add_argument(
        "--port",
        type=int,
        default=3306,
        metavar="PORT",
        help="MySQL port (default: %(default)s).",
    )
    db_group.add_argument(
        "--user",
        default=None,
        metavar="USER",
        help="MySQL username.",
    )
    db_group.add_argument(
        "--password",
        default=None,
        metavar="PASSWORD",
        help="MySQL password.",
    )
    db_group.add_argument(
        "--database",
        default=None,
        metavar="DATABASE",
        help="MySQL database / schema name.",
    )

    # Pipeline control
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Print scraped data to stdout instead of writing to MySQL.",
    )
    parser.add_argument(
        "--region",
        action="append",
        metavar="REGION",
        choices=["us", "europe", "japan", "all"],
        default=None,
        help=(
            "Region(s) to scrape. May be specified multiple times. "
            "Choices: us, europe, japan, all (default: all)."
        ),
    )
    parser.add_argument(
        "--timeout",
        type=int,
        default=15,
        metavar="SECONDS",
        help="HTTP request timeout in seconds (default: %(default)s).",
    )
    parser.add_argument(
        "--log-level",
        default="INFO",
        metavar="LEVEL",
        choices=["DEBUG", "INFO", "WARNING", "ERROR"],
        help="Logging verbosity (default: %(default)s).",
    )

    return parser


# ---------------------------------------------------------------------------
# Pretty-print helpers
# ---------------------------------------------------------------------------

_SEP = "─" * 72


def _print_program(prog: ProgramData) -> None:
    """Print a single ProgramData record in a structured, readable format."""
    print(f"\n  {'Institution':12}: {prog.university_name}")
    print(f"  {'Programme':12}: {prog.program_name}")
    print(f"  {'Country':12}: {prog.country}  |  Region: {prog.region}")
    print(f"  {'Specializ.':12}: {prog.specialization}  |  Degree: {prog.degree_type}")
    print(f"  {'GPA ≥':12}: {prog.threshold_gpa}")
    if prog.threshold_gre_q is not None:
        print(
            f"  {'GRE':12}: Q≥{prog.threshold_gre_q}  V≥{prog.threshold_gre_v}  "
            f"AW≥{prog.threshold_gre_aw}"
        )
    if prog.threshold_jlpt:
        print(f"  {'JLPT ≥':12}: {prog.threshold_jlpt}")
    if prog.threshold_ielts is not None:
        print(f"  {'IELTS ≥':12}: {prog.threshold_ielts}")
    if prog.threshold_toefl is not None:
        print(f"  {'TOEFL ≥':12}: {prog.threshold_toefl}")
    if prog.threshold_ects_cs is not None:
        print(f"  {'ECTS CS ≥':12}: {prog.threshold_ects_cs}")
    thesis_str = "Yes" if prog.thesis_required else "No"
    print(f"  {'Thesis':12}: {thesis_str}")
    if prog.acceptance_rate is not None:
        print(f"  {'Accept rate':12}: {prog.acceptance_rate:.1%}")
    if prog.avg_accepted_gpa is not None:
        print(f"  {'Avg GPA':12}: {prog.avg_accepted_gpa}")
    print(f"  {'Source':12}: {prog.scrape_source_url}")


def _print_summary(programs: list[ProgramData]) -> None:
    """Print aggregated statistics for the collected programme data."""
    print(f"\n{_SEP}")
    print("  SUMMARY STATISTICS")
    print(_SEP)
    print(f"  Total programmes scraped : {len(programs)}")

    regions: dict[str, int] = {}
    for p in programs:
        regions[p.region] = regions.get(p.region, 0) + 1
    for region, count in sorted(regions.items()):
        print(f"    {region:<14}: {count}")

    thesis_count = sum(1 for p in programs if p.thesis_required)
    print(f"  Thesis required          : {thesis_count} / {len(programs)}")

    gpas = [p.threshold_gpa for p in programs if p.threshold_gpa is not None]
    if gpas:
        print(f"  GPA threshold range      : {min(gpas):.2f} – {max(gpas):.2f}")

    rates = [p.acceptance_rate for p in programs if p.acceptance_rate is not None]
    if rates:
        avg_rate = sum(rates) / len(rates)
        print(f"  Mean acceptance rate     : {avg_rate:.1%}")
    print(_SEP)


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main() -> int:
    """
    Entry-point.

    Returns
    -------
    int
        Exit code (0 = success, 1 = error).
    """
    parser = _build_parser()
    args = parser.parse_args()

    _configure_logging(args.log_level)
    logger = logging.getLogger("main")

    # Normalise regions
    regions: list[str] = args.region or ["all"]

    # Validate DB args when not dry-run
    if not args.dry_run:
        missing = [
            flag
            for flag, val in [
                ("--user", args.user),
                ("--password", args.password),
                ("--database", args.database),
            ]
            if val is None
        ]
        if missing:
            parser.error(
                f"The following arguments are required when not using --dry-run: "
                f"{', '.join(missing)}"
            )

    # ------------------------------------------------------------------
    # Run pipeline
    # ------------------------------------------------------------------
    logger.info("Starting MS Admissions Matrix Engine (regions=%s).", regions)

    pipeline = ScraperPipeline(timeout=args.timeout, regions=regions)
    try:
        programs: list[ProgramData] = pipeline.run_and_validate()
    except Exception as exc:
        logger.error("Pipeline failed: %s", exc, exc_info=True)
        return 1

    if not programs:
        logger.warning("Pipeline returned no valid programmes. Nothing to do.")
        return 0

    # ------------------------------------------------------------------
    # Output
    # ------------------------------------------------------------------
    if args.dry_run:
        print(f"\n{_SEP}")
        print("  DRY-RUN MODE — data NOT written to database")
        print(_SEP)
        for prog in programs:
            _print_program(prog)
        _print_summary(programs)
        return 0

    # ------------------------------------------------------------------
    # Write to DB
    # ------------------------------------------------------------------
    logger.info(
        "Writing %d programme(s) to MySQL database '%s' on %s:%s.",
        len(programs),
        args.database,
        args.host,
        args.port,
    )
    writer: Optional[DatabaseWriter] = None
    try:
        writer = DatabaseWriter(
            host=args.host,
            user=args.user,
            password=args.password,
            database=args.database,
            port=args.port,
        )
        writer.upsert_all(programs)
        logger.info("All records written successfully.")
    except Exception as exc:
        logger.error("Database write failed: %s", exc, exc_info=True)
        return 1
    finally:
        if writer:
            writer.close()

    _print_summary(programs)
    return 0


if __name__ == "__main__":
    sys.exit(main())
