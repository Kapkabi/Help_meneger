import sqlite3

import pytest

from core.exceptions import DatabaseMigrationError
from data.database import Database


def test_migration_creates_expected_tables(db):
    tables = {
        row[0]
        for row in db.connection.execute("SELECT name FROM sqlite_master WHERE type = 'table'")
    }
    expected = {
        "schema_version",
        "users",
        "categories",
        "recipes",
        "ingredients",
        "recipe_ingredients",
        "units",
        "tags",
        "recipe_tags",
        "ratings",
        "recipe_history",
    }
    assert expected.issubset(tables)


def test_migration_records_schema_version(db):
    version = db.connection.execute("SELECT MAX(version) FROM schema_version").fetchone()[0]
    assert version == 3


def test_recipes_have_is_synced_column(db):
    columns = {row[1] for row in db.connection.execute("PRAGMA table_info(recipes)")}
    assert "is_synced" in columns


def test_recipes_id_is_text_uuid_not_autoincrement_integer(db):
    columns = {row[1]: row[2] for row in db.connection.execute("PRAGMA table_info(recipes)")}
    assert columns["id"] == "TEXT"


def test_recipe_fk_tables_use_text_recipe_id(db):
    for table in ("recipe_ingredients", "recipe_tags", "ratings", "recipe_history"):
        columns = {row[1]: row[2] for row in db.connection.execute(f"PRAGMA table_info({table})")}
        assert columns["recipe_id"] == "TEXT", f"{table}.recipe_id should be TEXT"


def test_builtin_categories_seeded(db):
    names = {row[0] for row in db.connection.execute("SELECT name FROM categories")}
    assert {"Завтрак", "Обед", "Ужин", "Десерт", "Другое"}.issubset(names)


def test_builtin_units_seeded(db):
    names = {row[0] for row in db.connection.execute("SELECT name FROM units")}
    assert {"гр", "шт", "л"}.issubset(names)


def test_migration_is_idempotent(tmp_path):
    db_path = tmp_path / "idempotent.db"
    first = Database(db_path=db_path)
    first.connect()
    first.close()

    second = Database(db_path=db_path)
    second.connect()
    version = second.connection.execute("SELECT MAX(version) FROM schema_version").fetchone()[0]
    assert version == 3
    second.close()


def test_corrupted_database_raises_app_error(tmp_path):
    bad_file = tmp_path / "corrupted.db"
    bad_file.write_bytes(b"not a sqlite database")

    database = Database(db_path=bad_file)
    with pytest.raises(DatabaseMigrationError):
        database.connect()
