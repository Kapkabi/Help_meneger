"""Connection management and schema migrations for the SQLite database.

This is the only module that knows about the on-disk schema version table.
Repositories receive a Database instance and use Database.connection /
Database.transaction() rather than opening their own connections.
"""

import sqlite3
from contextlib import contextmanager
from pathlib import Path
from typing import Optional, Union

from core.exceptions import DatabaseConnectionError, DatabaseMigrationError
from data import migrations

DEFAULT_DB_PATH = Path(__file__).resolve().parent.parent / "recipes.db"


class Database:
    def __init__(self, db_path: Optional[Union[str, Path]] = None):
        self.db_path = str(db_path) if db_path is not None else str(DEFAULT_DB_PATH)
        self._conn: Optional[sqlite3.Connection] = None

    def connect(self) -> sqlite3.Connection:
        try:
            conn = sqlite3.connect(self.db_path)
            conn.row_factory = sqlite3.Row
            conn.execute("PRAGMA foreign_keys = ON")
        except sqlite3.Error as exc:
            raise DatabaseConnectionError(
                f"Не удалось открыть базу данных «{self.db_path}»: {exc}"
            ) from exc
        self._conn = conn
        self._migrate()
        return self._conn

    @property
    def connection(self) -> sqlite3.Connection:
        if self._conn is None:
            self.connect()
        return self._conn

    @contextmanager
    def transaction(self):
        conn = self.connection
        try:
            yield conn
            conn.commit()
        except Exception:
            conn.rollback()
            raise

    def _current_version(self) -> int:
        self._conn.execute(
            "CREATE TABLE IF NOT EXISTS schema_version ("
            "version INTEGER NOT NULL, applied_at TEXT NOT NULL DEFAULT (datetime('now')))"
        )
        row = self._conn.execute("SELECT MAX(version) FROM schema_version").fetchone()
        return row[0] or 0

    def _migrate(self) -> None:
        try:
            current = self._current_version()
            for migration in migrations.ALL_MIGRATIONS:
                if migration.VERSION > current:
                    migration.up(self._conn)
                    self._conn.execute(
                        "INSERT INTO schema_version (version) VALUES (?)",
                        (migration.VERSION,),
                    )
                    self._conn.commit()
        except sqlite3.DatabaseError as exc:
            # Covers corrupted database files ("file is not a database", etc.)
            raise DatabaseMigrationError(
                f"Не удалось применить миграции схемы БД (возможно, файл повреждён): {exc}"
            ) from exc

    def close(self) -> None:
        if self._conn is not None:
            self._conn.close()
            self._conn = None
