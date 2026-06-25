package com.kapkabi.helpmeneger.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Mirrors the desktop `ingredients` table: a dictionary of distinct ingredient names. */
@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
)
