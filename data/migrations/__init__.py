from . import migration_0001_initial, migration_0002_units, migration_0003_recipe_is_synced

# Ordered list of migrations, applied in sequence based on VERSION.
# To add a new migration: create migration_00NN_description.py with a
# module-level VERSION (int) and an up(conn) function, then register it here.
ALL_MIGRATIONS = [
    migration_0001_initial,
    migration_0002_units,
    migration_0003_recipe_is_synced,
]
