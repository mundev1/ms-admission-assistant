"""
db_writer.py
------------
Writes validated :class:`~scrapers.base_scraper.ProgramData` records to a
MySQL database.

Schema assumptions
~~~~~~~~~~~~~~~~~~
The writer expects two tables to exist (DDL not run here — use your migration
tool):

.. code-block:: sql

    -- Lookup table mapping region codes to framework IDs
    CREATE TABLE IF NOT EXISTS country_frameworks (
        id     INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
        region VARCHAR(32)  NOT NULL UNIQUE,
        label  VARCHAR(128) NOT NULL
    );

    -- Main programme table
    CREATE TABLE IF NOT EXISTS ms_programs (
        id                 INT           NOT NULL AUTO_INCREMENT PRIMARY KEY,
        university_name    VARCHAR(256)  NOT NULL,
        program_name       VARCHAR(256)  NOT NULL,
        country            CHAR(2)       NOT NULL,
        region             VARCHAR(32)   NOT NULL,
        framework_id       INT           DEFAULT NULL,
        specialization     VARCHAR(128)  NOT NULL,
        degree_type        VARCHAR(32)   NOT NULL DEFAULT 'MS',
        threshold_gpa      DECIMAL(4,2)  DEFAULT NULL,
        threshold_gre_q    SMALLINT      DEFAULT NULL,
        threshold_gre_v    SMALLINT      DEFAULT NULL,
        threshold_gre_aw   DECIMAL(3,1)  DEFAULT NULL,
        threshold_jlpt     VARCHAR(8)    DEFAULT NULL,
        threshold_ielts    DECIMAL(3,1)  DEFAULT NULL,
        threshold_toefl    SMALLINT      DEFAULT NULL,
        threshold_ects_cs  SMALLINT      DEFAULT NULL,
        thesis_required    TINYINT(1)    NOT NULL DEFAULT 0,
        acceptance_rate    DECIMAL(5,4)  DEFAULT NULL,
        avg_accepted_gpa   DECIMAL(4,2)  DEFAULT NULL,
        scrape_source_url  TEXT          DEFAULT NULL,
        updated_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                         ON UPDATE CURRENT_TIMESTAMP,
        UNIQUE KEY uq_univ_prog (university_name(200), program_name(200))
    );

Usage
~~~~~
::

    from pipeline.db_writer import DatabaseWriter
    from pipeline.scraper_pipeline import ScraperPipeline

    pipeline = ScraperPipeline()
    programs = pipeline.run_and_validate()

    writer = DatabaseWriter(
        host="localhost", user="root", password="secret", database="admissions"
    )
    writer.upsert_all(programs)
    writer.close()
"""

from __future__ import annotations

import logging
from typing import Optional

import mysql.connector
from mysql.connector import Error as MySQLError
from mysql.connector.connection import MySQLConnection
from mysql.connector.cursor import MySQLCursor

from scrapers.base_scraper import ProgramData

logger = logging.getLogger(__name__)

# ---------------------------------------------------------------------------
# SQL constants
# ---------------------------------------------------------------------------

_UPSERT_SQL = """
INSERT INTO ms_programs (
    university_name,
    program_name,
    country,
    region,
    framework_id,
    specialization,
    degree_type,
    threshold_gpa,
    threshold_gre_q,
    threshold_gre_v,
    threshold_gre_aw,
    threshold_jlpt,
    threshold_ielts,
    threshold_toefl,
    threshold_ects_cs,
    thesis_required,
    acceptance_rate,
    avg_accepted_gpa,
    scrape_source_url
)
VALUES (
    %(university_name)s,
    %(program_name)s,
    %(country)s,
    %(region)s,
    %(framework_id)s,
    %(specialization)s,
    %(degree_type)s,
    %(threshold_gpa)s,
    %(threshold_gre_q)s,
    %(threshold_gre_v)s,
    %(threshold_gre_aw)s,
    %(threshold_jlpt)s,
    %(threshold_ielts)s,
    %(threshold_toefl)s,
    %(threshold_ects_cs)s,
    %(thesis_required)s,
    %(acceptance_rate)s,
    %(avg_accepted_gpa)s,
    %(scrape_source_url)s
)
ON DUPLICATE KEY UPDATE
    country            = VALUES(country),
    region             = VALUES(region),
    framework_id       = VALUES(framework_id),
    specialization     = VALUES(specialization),
    degree_type        = VALUES(degree_type),
    threshold_gpa      = VALUES(threshold_gpa),
    threshold_gre_q    = VALUES(threshold_gre_q),
    threshold_gre_v    = VALUES(threshold_gre_v),
    threshold_gre_aw   = VALUES(threshold_gre_aw),
    threshold_jlpt     = VALUES(threshold_jlpt),
    threshold_ielts    = VALUES(threshold_ielts),
    threshold_toefl    = VALUES(threshold_toefl),
    threshold_ects_cs  = VALUES(threshold_ects_cs),
    thesis_required    = VALUES(thesis_required),
    acceptance_rate    = VALUES(acceptance_rate),
    avg_accepted_gpa   = VALUES(avg_accepted_gpa),
    scrape_source_url  = VALUES(scrape_source_url),
    updated_at         = CURRENT_TIMESTAMP
"""

_FRAMEWORK_QUERY = """
SELECT id FROM country_frameworks WHERE region = %(region)s LIMIT 1
"""


class DatabaseWriter:
    """
    Handles all MySQL write operations for the MS admissions pipeline.

    Parameters
    ----------
    host:
        MySQL server hostname or IP address.
    user:
        MySQL username.
    password:
        MySQL password.
    database:
        Target database / schema name.
    port:
        MySQL port (default ``3306``).
    """

    def __init__(
        self,
        host: str,
        user: str,
        password: str,
        database: str,
        port: int = 3306,
    ) -> None:
        self._host = host
        self._user = user
        self._password = password
        self._database = database
        self._port = port
        self._conn: Optional[MySQLConnection] = None
        self._connect()

    # ------------------------------------------------------------------
    # Connection management
    # ------------------------------------------------------------------

    def _connect(self) -> None:
        """Establish the MySQL connection."""
        try:
            self._conn = mysql.connector.connect(
                host=self._host,
                user=self._user,
                password=self._password,
                database=self._database,
                port=self._port,
                autocommit=False,
                charset="utf8mb4",
                collation="utf8mb4_unicode_ci",
                connection_timeout=10,
            )
            logger.info(
                "Connected to MySQL database '%s' on %s:%s.",
                self._database,
                self._host,
                self._port,
            )
        except MySQLError as exc:
            logger.error("Failed to connect to MySQL: %s", exc)
            raise

    def close(self) -> None:
        """Close the MySQL connection gracefully."""
        if self._conn and self._conn.is_connected():
            self._conn.close()
            logger.info("MySQL connection closed.")

    def _cursor(self) -> MySQLCursor:
        """Return a fresh cursor, reconnecting if the connection was lost."""
        if not self._conn or not self._conn.is_connected():
            logger.warning("MySQL connection lost — attempting reconnect.")
            self._connect()
        return self._conn.cursor(dictionary=True)  # type: ignore[union-attr]

    # ------------------------------------------------------------------
    # Public write methods
    # ------------------------------------------------------------------

    def get_framework_id(self, region: str) -> Optional[int]:
        """
        Retrieve the primary-key ID for a region from ``country_frameworks``.

        Parameters
        ----------
        region:
            One of ``"US_CANADA"``, ``"EUROPE"``, or ``"JAPAN"``.

        Returns
        -------
        int or None
            The ``id`` column value, or ``None`` if the region is not found.
        """
        cursor: Optional[MySQLCursor] = None
        try:
            cursor = self._cursor()
            cursor.execute(_FRAMEWORK_QUERY, {"region": region})
            row = cursor.fetchone()
            if row:
                return int(row["id"])
            logger.warning(
                "No framework record found for region '%s'.", region
            )
            return None
        except MySQLError as exc:
            logger.error("Error querying country_frameworks: %s", exc)
            return None
        finally:
            if cursor:
                cursor.close()

    def upsert_program(self, program: ProgramData) -> None:
        """
        Insert or update a single programme record.

        Uses ``INSERT … ON DUPLICATE KEY UPDATE`` so that re-running the
        scraper is idempotent — existing rows are refreshed rather than
        duplicated.

        Parameters
        ----------
        program:
            Validated :class:`ProgramData` record to persist.

        Raises
        ------
        mysql.connector.Error
            Re-raised after logging and rollback if the query fails.
        """
        framework_id = self.get_framework_id(program.region)

        params: dict = {
            "university_name": program.university_name,
            "program_name": program.program_name,
            "country": program.country,
            "region": program.region,
            "framework_id": framework_id,
            "specialization": program.specialization,
            "degree_type": program.degree_type,
            "threshold_gpa": program.threshold_gpa,
            "threshold_gre_q": program.threshold_gre_q,
            "threshold_gre_v": program.threshold_gre_v,
            "threshold_gre_aw": program.threshold_gre_aw,
            "threshold_jlpt": program.threshold_jlpt,
            "threshold_ielts": program.threshold_ielts,
            "threshold_toefl": program.threshold_toefl,
            "threshold_ects_cs": program.threshold_ects_cs,
            "thesis_required": int(program.thesis_required),
            "acceptance_rate": program.acceptance_rate,
            "avg_accepted_gpa": program.avg_accepted_gpa,
            "scrape_source_url": program.scrape_source_url or None,
        }

        cursor: Optional[MySQLCursor] = None
        try:
            cursor = self._cursor()
            cursor.execute(_UPSERT_SQL, params)
            # Commit is deferred to upsert_all for batch efficiency
            logger.debug(
                "Upserted: %s — %s", program.university_name, program.program_name
            )
        except MySQLError as exc:
            logger.error(
                "Upsert failed for %s — %s: %s",
                program.university_name,
                program.program_name,
                exc,
            )
            if self._conn and self._conn.is_connected():
                self._conn.rollback()
            raise
        finally:
            if cursor:
                cursor.close()

    def upsert_all(self, programs: list[ProgramData]) -> None:
        """
        Upsert a batch of programme records inside a single transaction.

        On success the transaction is committed.  On any error the full
        batch is rolled back and the exception is re-raised after logging.

        Parameters
        ----------
        programs:
            List of :class:`ProgramData` records to persist.
        """
        if not programs:
            logger.warning("upsert_all called with an empty list — nothing to write.")
            return

        logger.info("Starting batch upsert of %d record(s).", len(programs))
        success_count = 0

        try:
            for prog in programs:
                self.upsert_program(prog)
                success_count += 1

            if self._conn and self._conn.is_connected():
                self._conn.commit()
            logger.info(
                "Batch upsert committed successfully — %d record(s) written.",
                success_count,
            )
        except MySQLError as exc:
            logger.error(
                "Batch upsert failed after %d record(s) — rolling back. Error: %s",
                success_count,
                exc,
            )
            if self._conn and self._conn.is_connected():
                self._conn.rollback()
            raise
