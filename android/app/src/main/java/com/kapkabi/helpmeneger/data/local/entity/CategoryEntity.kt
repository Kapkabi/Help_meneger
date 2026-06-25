package com.kapkabi.helpmeneger.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Mirrors the desktop `categories` table (data/migrations/migration_0001_initial.py).
 * Integer-keyed: categories are a small built-in dictionary, not created offline per device.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "is_builtin")
    val isBuiltin: Boolean = false,
)
