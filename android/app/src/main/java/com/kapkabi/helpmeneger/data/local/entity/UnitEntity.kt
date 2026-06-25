package com.kapkabi.helpmeneger.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Mirrors the desktop `units` table (data/migrations/migration_0002_units.py).
 * Dictionary of measurement units (гр/шт/л/...), grown via get-or-create from the UI.
 */
@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
)
