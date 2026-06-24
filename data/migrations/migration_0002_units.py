"""Adds a units-of-measurement dictionary backing the ingredient form dropdown.

Mirrors the ingredients table: unique (case-insensitive) names, grown via
get_or_create as users type new units in the UI.
"""

VERSION = 2


def up(conn):
    conn.executescript(
        """
        CREATE TABLE IF NOT EXISTS units (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL UNIQUE COLLATE NOCASE
        );

        INSERT OR IGNORE INTO units (name) VALUES ('гр'), ('шт'), ('л');
        """
    )
