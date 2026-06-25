package com.kapkabi.helpmeneger.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Mirrors the desktop `tags` table. Reserved for a future tagging feature — no UI yet. */
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
)
