"""Adds is_synced to recipes — a flag reserved for a future cloud-sync feature."""

VERSION = 3


def up(conn):
    conn.executescript(
        """
        ALTER TABLE recipes ADD COLUMN is_synced INTEGER NOT NULL DEFAULT 0;
        """
    )
