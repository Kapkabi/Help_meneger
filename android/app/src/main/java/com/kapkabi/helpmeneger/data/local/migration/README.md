# Migrations

One file per schema version bump (e.g. `Migration1To2.kt`), each exporting a single
`androidx.room.migration.Migration` val. Register new migrations in
`AppDatabase.MIGRATIONS`. Never edit a migration after it has shipped — add a new one instead,
exactly like the desktop project's `data/migrations/migration_NNNN_*.py` files.
